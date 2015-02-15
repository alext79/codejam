package codejam.sevensegment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.RuntimeErrorException;


public class SevenSegmentDisplay {
	public static int bad = 1<<9;
	public static int tooMany = 1<<10;
	static final int A = 1 << 6;
	static final int B = 1 << 5;
	static final int C = 1 << 4;
	static final int D = 1 << 3;
	static final int E = 1 << 2;
	static final int F = 1 << 1;
	static final int G = 1;
	static final int zero = A | B | C | D | E | F;
	static final int one =  B|C;
	static final int two = A | B | D | E | G;
	static final int three = A | B | C | D | G;
	static final int four = B | C | F | G;
	static final int five = A | C | D | F | G;
	static final int six = A | C | D | E | F | G;
	static final int seven = A | B | C;
	static final int eight = A | B | C | D | E | F | G;
	static final int nine = A | B | C | D | F | G;
	static final int[] vals = { zero, one, two, three, four, five, six, seven,eight, nine };
	private Set<Integer> values;
	private int[] confs;
	public SevenSegmentDisplay(String states) {
		values = new HashSet<Integer>();
		for (int i : vals)
			values.add(i);
		confs = setConfs(states);
	}

	public int solve() {
		Set<Integer> possibleNextState = new HashSet<Integer>();
		int[] ledsToTest = ledsToTest(this.confs);
		int toReturn = bad;
		int valNext=bad;
		List<Integer> brokenLeds = generateBrokenLedsSubSet(ledsToTest);
		if(brokenLeds.size()==1)
			return next(confs[confs.length-1]);
		int brokConf=0;
		for(int i : brokenLeds) 
		{
			int next = testHyp(this.confs, i);
			if(next == tooMany)
				return bad;
			if(next!=bad)
			{	
				toReturn = next &(~i);
				possibleNextState.add(toReturn);
				if(possibleNextState.size()>1)
					return bad;
				brokConf=i;
				valNext=next;
			}
		
			
		}
		if(valNext!= bad) {
			checkSolution(valNext, brokConf);
		}
		return toReturn;
		
		
	}

	private void checkSolution(int valNext, int brokConf) {
		int start =valNext;
		for(int i =0;i<confs.length;i++){
			start=prev(start);
		}
		for(int i =0;i<confs.length;i++)
		{
			int toCheck =start &(~brokConf);
			if(toCheck!=confs[i]){
				System.out.println("step: "+i+" is possible "+isPossible(toCheck, start, brokConf));
				System.out.println("start : "+toLeds(start));
				System.out.println("target: "+toLeds(toCheck));
				System.out.println("con   : "+toLeds(confs[i]));
				System.out.println("broke : "+toLeds(brokConf));
				throw new RuntimeErrorException(new Error("asdf"));
			}
				start=next(start);
			
		}
	}
	
	
	private int[] setConfs(String states) {
		String[] statesSplit = states.split(" ");
		int[] confs = new int[statesSplit.length-1];
		for(int i =1;i<statesSplit.length;i++)
			confs[i-1]=ledConf(statesSplit[i]);
		return confs;
	}

	public String toLeds(int i) {
		String s = "";
		for(int l : leds)
			s+=(l&i)>0?1:0;
		return s;
	}



	public boolean isPossible(int cur, int candidate, int broken) {
		int tooManyOnes = (~candidate)&cur;
		if(tooManyOnes!=0)
			return false;
		int missing = (~cur) & candidate;
		int possible = broken & missing;

		return missing == possible;
	}

	private int next(int state) {
		switch (state) {
		case zero:
			return nine;
		case one:
			return zero;
		case two:
			return one;
		case three:
			return two;
		case four:
			return three;
		case five:
			return four;
		case six:
			return five;
		case seven:
			return six;
		case eight:
			return seven;
		case nine:
			return eight;

		default:
			break;
		}

		throw new RuntimeErrorException(new Error("invalid state"));
	}


	private int prev(int state) {
		switch (state) {
		case zero:
			return one;
		case one:
			return two;
		case two:
			return three;
		case three:
			return four;
		case four:
			return five;
		case five:
			return six;
		case six:
			return seven;
		case seven:
			return eight;
		case eight:
			return nine;
		case nine:
			return zero;

		default:
			break;
		}

		throw new RuntimeErrorException(new Error("invalid state "+state+" to led "+toLeds(state)));
	}

	
	
	public int ledConf(String s) {
		int conf = 0;
		for (int i = 0; i < 7; i++)
			if (s.charAt(i) == '1')
				conf = conf | leds[i];
		return conf;

	}
 int testHyp(int[] stateSequence, int brokenLed) {
		int found = 0;
		int actualState = 0;
		int nextToReturn =bad;
		Set<Integer> fval = new HashSet<Integer>();
		for (int v : values) 
		{	
			boolean ok = true;
			if (isPossible(stateSequence[0], v, brokenLed)) 
			{
				actualState = v;
				for (int i = 1; i < stateSequence.length; i++) {
						int next = next(actualState);
						if (!isPossible(stateSequence[i], next, brokenLed)) {
							ok = false;
							break;
						}
						actualState = next;
					
				}

				if (ok)
				{	nextToReturn = next(actualState);
					fval.add(nextToReturn&(~brokenLed));
					found=fval.size();
		
				}
			}
		}
		if(found==1)
			return nextToReturn;
		if(found>1)
			return tooMany;
		return bad;

	}
	
	
	public List<Integer> generateBrokenLedsSubSet(int[] possibleBadLeds) {
		
		List<Integer> subset = new ArrayList<Integer>();
		int subsetSize = (int) Math.pow(2.0, possibleBadLeds.length);
		for (int i = 0; i < subsetSize; i++) {
			Integer curList = 0;
			for (int k = 0; k < possibleBadLeds.length; k++) {
				if (getBit(i, k) > 0)
					curList = curList | (possibleBadLeds[k]);
			}
			subset.add(curList);
		}

		return subset;
	}
	
	int getBit(int n, int k) {
		return (n >> k) & 1;
	}

	public int[] ledsToTest(int[] confs) {
		int conf = 0;
		for (int i : confs)
			conf = conf | i;
		boolean[] toTestFlag = { true, true, true, true, true, true, true };

		for (int i = 0; i < leds.length; i++)
			if ((conf & leds[i]) >= 1)
				toTestFlag[i] = false;
		int c = 0;
		for (boolean b : toTestFlag)
			if (b)
				c++;
		int[] ledsToTest = new int[c];
		int j = 0;
		for (int i = 0; i < toTestFlag.length; i++)
			if (toTestFlag[i])
				ledsToTest[j++] = leds[i];
		return ledsToTest;

	}

	static int[] leds = { A, B, C, D, E, F, G };

	public static void main(String[] args) throws NumberFormatException, IOException {
	
		String resFile =args[0]+".out";
		FileReader ifi = new FileReader(args[0]);
		BufferedReader r = new BufferedReader(ifi);
		OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
				resFile));
		BufferedWriter wr = new BufferedWriter(w);
		int n = 0;
		n = Integer.parseInt(r.readLine().trim());
		try {
		for (int i = 0; i < n; i++) {
			System.out.println("case "+i);
			SevenSegmentDisplay s = new SevenSegmentDisplay( r.readLine());
			int solved = s.solve();
			String result = solved==bad?"ERROR!":s.toLeds(solved);
			wr.write("Case #" + (i + 1) + ": " + result);
			wr.newLine();
		}}
		catch(RuntimeErrorException e){
			e.printStackTrace();
			
		}
		finally{
		r.close();
		wr.close();
		}
		
		
		
	}
}
