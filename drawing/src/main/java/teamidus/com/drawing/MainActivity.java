package teamidus.com.drawing;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import teamidus.com.drawing.external.google.PieChart;

/**
 * Created by nyh0111 on 2018-04-05.
 */

public class MainActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setupPieChart(getResources());
    
        final RoundPeakProgressBar progressBar = findViewById(R.id.progress_bar);
        
        ValueAnimator animator = readyProgressBarAnimation();
        
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });
        
        animator.start();
    }
    
    private void setupPieChart(Resources res) {
        final PieChart pie = this.findViewById(R.id.Pie);
        pie.addItem("Agamemnon", 2, res.getColor(R.color.seafoam));
        pie.addItem("Bocephus", 3.5f, res.getColor(R.color.chartreuse));
        pie.addItem("Calliope", 2.5f, res.getColor(R.color.emerald));
        pie.addItem("Daedalus", 3, res.getColor(R.color.bluegrass));
        pie.addItem("Euripides", 1, res.getColor(R.color.turquoise));
        pie.addItem("Ganymede", 3, res.getColor(R.color.slate));
        
        findViewById(R.id.Reset).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pie.setCurrentItem(0);
            }
        });
    }
    
    private ValueAnimator readyProgressBarAnimation() {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 100f);
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.RESTART);
        return anim;
    }
}