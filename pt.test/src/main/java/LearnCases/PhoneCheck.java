package LearnCases;


import java.util.*;

public class PhoneCheck {
	public static void main(String[] args) {
		final int input[] = {2389,8922,3382,6982,5231,8934,8923,7593
							,4322,7922,6892,5224,4829,3829,8934,8922
							,6892,6872,4682,6723,8923,3492,9527,8923
							,7593,7698,7593,7593,7593,8922,9527,4322
							,8934,4322,3382,5231,5231,4682,9527,9527};
		
		int sort[] = new int[1000];
		//Set all bit to 0
		for(int index = 0; index < sort.length; index++){
			sort[index] = 0;
		}
		Map<Integer,Integer> numCountMap = new HashMap<Integer,Integer>();
		for(int number : input){
			//Every number takes 2 bit.
			int existTimes = (sort[number >>> 4] >>> (2 * (number % 16))) & (1 | 1 << 1);
			//Increase counter in sort array.
			if(existTimes <= ((1 | 1 << 1) - 1)){
				existTimes++;
				//set two bit zero
				sort[number >>> 4] &= ~((1 | 1 << 1) << (2 * (number % 16)));
				//set increased bit value
				sort[number >>> 4] |= existTimes << (2 * (number % 16));
				//set <number, counter> into two maps.
				if((1 | 1 << 1) == existTimes){
					numCountMap.put(number, existTimes);
				}
			}
			else{
				//Time >= 3, increase the counter in treemap.
				if((1 | 1 << 1) == existTimes){
					int mapCounter = numCountMap.get(number).intValue();
					mapCounter++;
					numCountMap.put(number, mapCounter);
				}
			}
		}
 
		List<CounterNumber> counterList = new LinkedList<CounterNumber>();
		for(Integer number : numCountMap.keySet()){
			counterList.add(new CounterNumber(numCountMap.get(number), number));
		}
		Collections.sort(counterList);
		for(CounterNumber counterNumber : counterList){
			System.out.println(counterNumber.getCounter() + "----" + counterNumber.getNumber());
		}
	}
}

class CounterNumber implements Comparable<CounterNumber>{
	private Integer counter;
	private Integer number;
	public CounterNumber(Integer counter, Integer number){
		this.counter = counter;
		this.number  = number;
	}
	
	public Integer getCounter(){
		return this.counter;
	}
	public Integer getNumber(){
		return this.number;
	}
	@Override
	public int compareTo(CounterNumber counterNumber){
		return counterNumber.getCounter().compareTo(this.getCounter());
	}
}