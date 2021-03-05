package LearnCases;

import java.util.Arrays;
import java.util.List;

public class Algorithm {
	static List<Integer> arr=Arrays.asList(2,4,5,6);
	static int[] arr1= {2,4,5,6};
	public static void main(String args[]) {
		System.out.println(arr);
		System.out.println(arr1);
		for(int i=0;i<arr.size();i++) {
			System.out.println(arr.get(i));
		}
		
		for(int i=0;i<arr1.length;i++) {
			System.out.println(arr1[i]);
		}
		
		for(int i=0;i<arr.size()-1;i++) {
			for(int j=1;j<arr.size();j++) {
				if(arr.get(i)+arr.get(j)==11) {
					
					System.out.println("找到了");
					break;
				}
			}
		}
		
		for(int i=0;i<arr.size()-1;i++) {
			for(int j=i;j<arr.size();j++) {
				if(arr.get(i)+arr.get(j)==11) {
					
					System.out.println("找到了");
					break;
				}
			}
		}
		
	}
}
