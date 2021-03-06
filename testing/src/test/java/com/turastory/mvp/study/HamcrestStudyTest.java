package com.turastory.mvp.study;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

public class HamcrestStudyTest {
    
    @Test
    public void test_isAndNot() {
        assertThat(true, is(true));
        assertThat(true, is(not(false)));
    }
    
    @Test
    public void test_chainMatchers() {
        assertThat("test", anyOf(is("testing"), containsString("est")));
        assertThat("android testing", allOf(not(isEmptyOrNullString()), startsWith("android")));
    }
    
    @Test
    public void test_instanceOf() {
        String result = "";
        int item = 10;
        
        // JUnit 4
        assertTrue(result instanceof String);
        
        // With hamcrest
        assertThat(result, instanceOf(String.class));
        assertThat(item, not(instanceOf(String.class)));
    }
    
    @Test
    public void test_listMatchers() {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        
        assertThat(integers, hasSize(greaterThan(3)));
        
        // contains - should match all elements of the list.. why?
        assertThat(integers, contains(1, 2, 3, 4, 5));
        assertThat(integers, containsInAnyOrder(5, 3, 1, 4, 2));
        
        assertThat(integers, everyItem(not(0)));
    }
}
