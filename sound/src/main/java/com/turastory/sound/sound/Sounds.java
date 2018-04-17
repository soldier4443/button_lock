package com.turastory.sound.sound;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tura on 2018-04-12.
 * <p>
 * Singleton class to manage sounds.
 */
public class Sounds {
    private AtomicBoolean isReady = new AtomicBoolean(false);
    private AtomicInteger playStreamId = new AtomicInteger(0);
    private SoundPool pool;
    private Map<String, RawSound> rawSounds;
    
    private boolean sequentialPlayback = false;
    private AtomicBoolean playingSequential = new AtomicBoolean(false);
    private Queue<RawSound> sequentialPlayQueue = new LinkedBlockingQueue<>();
    
    private Sounds() {
    
    }
    
    public static Sounds ready() {
        return LazySingletonHolder.instance;
    }
    
    // Load using assets.
    public static Sounds.Builder preloadFromAssets(Context context) {
        return new Sounds.AssetsBuilder(context);
    }
    
    // Unload all sounds.
    public static void unloadAll() {
        SoundPool pool = Sounds.ready().pool;
        Stream.of(Sounds.ready().rawSounds)
            .forEach(entry -> pool.unload(entry.getValue().getSoundId()));
    }
    
    // Get duration of the sound.
    private static int getDuration(Consumer<MediaMetadataRetriever> dataSourceConfigurator) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        dataSourceConfigurator.accept(mmr);
        return Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }
    
    /**
     * Play the sound with name.
     *
     * @param name name of the sound to play.
     */
    public void play(String name) {
        use(name, sound -> {
            int success = pool.play(sound.getSoundId(), 1, 1, 1, 0, sound.getRate());
    
            if (success == 0) {
                Log.e("Sounds", "Failed to play sound '" + name + "'");
            } else {
                playStreamId.set(success);
            }
        });
    }
    
    /**
     * Stop recent played sound.
     */
    public void stop() {
        int streamId = playStreamId.get();
    
        if (streamId != 0) {
            pool.stop(streamId);
        }
    }
    
    public void playSequentially(String... names) {
        if (!sequentialPlayback) {
            Log.e("Sounds", "Sequential playback not configured.");
            return;
        }
        
        playingSequential.set(false);
        sequentialPlayQueue.clear();
        
        Stream.of(Arrays.asList(names)).forEach(name -> use(name, sound -> {
            sequentialPlayQueue.add(sound);
        }));
        
        if (sequentialPlayQueue.size() == 0)
            return;
        
        new Thread(() -> {
            int playTime = 0;
            playingSequential.set(true);
            
            // Initial
            RawSound rawSound = sequentialPlayQueue.remove();
            play(rawSound.getName());
            
            while (true) {
                if (!playingSequential.get()) {
                    stop();
                    break;
                }
                
                if (playTime > rawSound.getDuration()) {
                    playTime -= rawSound.getDuration();
                    
                    if (sequentialPlayQueue.size() > 0) {
                        rawSound = sequentialPlayQueue.remove();
                        play(rawSound.getName());
                    }
                }
                
                try {
                    Thread.sleep(2);
                    playTime += 2;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public void stopSequential() {
        if (!sequentialPlayback) {
            Log.e("Sounds", "Sequential playback not configured.");
            return;
        }
        
        playingSequential.set(false);
    }
    
    private void use(String name, Consumer<RawSound> soundConsumer) {
        if (!isReady.get()) {
            Log.e("Sounds", "Sounds has not ready yet.");
            return;
        }
        
        RawSound sound = rawSounds.get(name);
        
        if (sound != null) {
            soundConsumer.accept(sound);
        } else {
            throw new RuntimeException("Sound with " + name + " does not exist.");
        }
    }
    
    private static class LazySingletonHolder {
        private static Sounds instance = new Sounds();
    }
    
    /**
     * Builder class for pre-load audio.
     */
    public static abstract class Builder {
        protected List<Sound> sounds;
        protected boolean sequentialPlayback;
        private int maxStreamSizeAtOnce;
    
        private Builder() {
            sounds = new ArrayList<>();
        }
        
        public Builder setMaxStreamSizeAtOnce(int maxStreamSizeAtOnce) {
            this.maxStreamSizeAtOnce = maxStreamSizeAtOnce;
            return this;
        }
    
        // Avoid calculating duration of the sounds.
        public Builder enableSequentialPlayback() {
            this.sequentialPlayback = true;
            return this;
        }
    
        // Should load sounds in another thread because
        // If there're a lot of sounds It may take some time to load them all.
        public void load(Runnable onLoadComplete) {
            SoundPool pool = createSoundPool(maxStreamSizeAtOnce);
            new Thread(() -> {
                Log.e("Sounds", "load sounds..");
                Sounds.ready().isReady.set(false);
                Map<String, RawSound> rawSounds = loadRawSounds(pool);
                Sounds.ready().pool = pool;
                Sounds.ready().rawSounds = rawSounds;
                Sounds.ready().isReady.set(true);
                Sounds.ready().sequentialPlayback = sequentialPlayback;
                Log.e("Sounds", "load sounds Complete!");
                if (onLoadComplete != null)
                    onLoadComplete.run();
            }).start();
        }
    
        public void load() {
            load(null);
        }
    
        public Builder addRawSound(Sound sound) {
            sounds.add(sound);
            return this;
        }
        
        public abstract Map<String, RawSound> loadRawSounds(SoundPool pool);
        
        private SoundPool createSoundPool(int maxStreamSize) {
            SoundPool sound;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
                
                sound = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
            } else {
                sound = new SoundPool(maxStreamSize, AudioManager.STREAM_MUSIC, 0);
            }
            
            return sound;
        }
    }
    
    private static class AssetsBuilder extends Builder {
    
        private Context context;
    
        private AssetsBuilder(Context context) {
            this.context = context;
        }
        
        @Override
        public Map<String, RawSound> loadRawSounds(SoundPool pool) {
            AssetManager assets = context.getAssets();
            
            return Stream.of(sounds).map(sound -> {
                try {
                    final AssetFileDescriptor afd = assets.openFd(sound.getPath());
    
                    int soundId = pool.load(afd, 1);
                    int duration = sequentialPlayback ?
                        getDuration(mmr -> mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())) :
                        0;
    
                    return new RawSound(sound.getName(), soundId, sound.getPitch(), duration);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toMap(RawSound::getName, rawSound -> rawSound));
        }
    }
    
    private static class FileBuilder extends Builder {
        @Override
        public Map<String, RawSound> loadRawSounds(SoundPool pool) {
            return Stream.of(sounds).map(sound -> {
                int soundId = pool.load(sound.getPath(), 1);
                int duration = sequentialPlayback ?
                    getDuration(mmr -> mmr.setDataSource(sound.getPath())) :
                    0;
                
                return new RawSound(sound.getName(), soundId, sound.getPitch(), duration);
            }).collect(Collectors.toMap(RawSound::getName, rawSound -> rawSound));
        }
    }
}
