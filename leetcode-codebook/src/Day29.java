import java.util.*;

/*@LC 1480
tag: Running sum (warm-up)
cue: Each position becomes the sum of everything up to it.
hook: Add the previous cell into this one.
time: O(n) time · O(1) extra
trap: Start at i = 1.
var: Exam-morning warm-up | Type this, LC 1 and LC 20 once each, then stop.
@*/
class LC1480 {
    public int[] runningSum(int[] nums) {
        for (int i = 1; i < nums.length; i++) nums[i] += nums[i - 1];   //*
        return nums;
    }
}

public class Day29 {
    public static void main(String[] args) {
        T.eq("1480a", new LC1480().runningSum(T.ints("[1,2,3,4]")), T.ints("[1,3,6,10]"));
        T.eq("1480b", new LC1480().runningSum(T.ints("[1,1,1,1,1]")), T.ints("[1,2,3,4,5]"));
        T.eq("1480c", new LC1480().runningSum(T.ints("[3,1,2,10,1]")), T.ints("[3,4,6,16,17]"));
        T.summary("Day29");
    }
}
