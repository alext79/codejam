package codejam.shoppingplan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ShoppingPlanDP {
	int numShops;
	int items;
	double gasPrice;
	HashMap<Integer, HashMap<Integer, Double>> distance = new HashMap<Integer, HashMap<Integer, Double>>();
	HashMap<String, Integer> itemMap = new HashMap<String, Integer>();
	private int perishableMask = 0;
	List<Shop> shops = new ArrayList<Shop>();

	int getBit(int n, int k) {
		return (n >> k) & 1;
	}

	public void addItems(String items) {
		for (String s : items.split(" ")) {
			boolean perishable = s.endsWith("!");
			s = s.replace("!", "");
			int num = itemMap.size();
			if (perishable)
				perishableMask = perishableMask | (1 << num);
			itemMap.put(s, num);

		}
	}

	public void addShop(String shop) {
		String[] parts = shop.split(" ");
		Shop s = new Shop();
		s.x = Integer.parseInt(parts[0]);
		s.y = Integer.parseInt(parts[1]);
		s.id = shops.size();
		for (int i = 2; i < parts.length; i++) {
			String[] partsItem = parts[i].split(":");
			Integer price = Integer.parseInt(partsItem[1]);
			Integer item = itemMap.get(partsItem[0]);
			s.items = s.items | (1 << item);
			s.prices.put(item, price);
		}
		shops.add(s);
	}

	private boolean checkForItem(int itemSet, int item) {
		return (1 & (itemSet >> item)) > 0;
	}

	public void computeDistanceMatrix() {
		for (int i = 0; i < shops.size(); i++) {
			Shop currShop = shops.get(i);
			HashMap<Integer, Double> currDist = new HashMap<Integer, Double>();
			distance.put(i, currDist);
			for (int k = i; k < shops.size(); k++)
				currDist.put(k, computeDistance(currShop, shops.get(k)));

		}

	}

	

	private int getNewItems(int currentSet, int target) {

		return target & (~currentSet);
	}

	private List<Integer> generateItemsSubSet(List<Integer> itemsToBuy) {
		List<Integer> subset = new ArrayList<Integer>();
		int subsetSize = (int) Math.pow(2.0, itemsToBuy.size());
		for (int i = 1; i < subsetSize; i++) {
			Integer curList = 0;
			for (int k = 0; k < itemsToBuy.size(); k++) {
				if (getBit(i, k) > 0)
					curList = curList | (1 << itemsToBuy.get(k));
			}
			subset.add(curList);
		}

		return subset;
	}
	
	private double cost(Shop s, int items) {
		double cost = 0;
		for (int i = 0; i < this.items; i++)
			if (checkForItem(items, i))
				cost += s.prices.get(i);
		return cost;
	}

	private boolean perishableLoad(int items) {
		return (items & perishableMask) > 0;
	}

	private double[][] buildMatrix() {
		int loads = (int) Math.pow(2.0, items);
		double[][] sols = new double[numShops][loads];
		for (int i = 0; i < numShops; i++)
			for (int j = 0; j < loads; j++)
				sols[i][j] = Double.MAX_VALUE;
		for (int i = 0; i < numShops; i++)
			sols[i][0] = computeDistance(shops.get(0), shops.get(i)) * gasPrice;
		return sols;
	}

	public double visit() {
		double[][] stateMatrix = buildMatrix();
		for (int sol = 0; sol < stateMatrix[0].length; sol++) {
			for (int i = 1; i < numShops; i++) {
				Shop s = shops.get(i);
				int allNewItems = getNewItems(sol, s.items);
				List<Integer> pi = new ArrayList<Integer>();
				for (int bi = 0; bi < items; bi++)
					if (checkForItem(allNewItems, bi))
						pi.add(bi);
				List<Integer> allPoss = generateItemsSubSet(pi);
				for(int load : allPoss)
				{
					int nextLoad = sol | load;
					int newItems = getNewItems(sol, nextLoad);
					boolean perishable = perishableLoad(newItems);
					double cost = cost(s, newItems);
					if (perishable) {
						computeCostsForPerishable(nextLoad, s, stateMatrix,
								stateMatrix[i][sol], cost);

					} else {
						for (int k = 0; k < numShops; k++) {
							double currCost = cost
									+ getDistance(shops.get(k).id, s.id)
									* gasPrice + stateMatrix[i][sol];
							stateMatrix[k][nextLoad] = Math.min(currCost,
									stateMatrix[k][nextLoad]);
						}
					}

				}
			}
		}
		return stateMatrix[0][stateMatrix[0].length - 1];

	}

	private double getDistance(int x, int y) {
		if (x > y)
			return getDistance(y, x);
		if (x == y)
			return 0;
		return distance.get(x).get(y);

	}

	private void computeCostsForPerishable(int nextLoad, Shop s,
			double[][] stateMatrix, double currentCost, double newItemsCost) {
		double costToTravelToStart = getDistance(s.id, shops.get(0).id)
				* gasPrice;
		double candidateCost = costToTravelToStart + newItemsCost+ currentCost;
		if (stateMatrix[0][nextLoad] > candidateCost) {
			stateMatrix[0][nextLoad] = candidateCost;
			for (int i = 1; i < numShops; i++)
				stateMatrix[i][nextLoad] = Math.min(
						stateMatrix[i][nextLoad],
						stateMatrix[0][nextLoad]+ getDistance(shops.get(0).id, shops.get(i).id)* gasPrice);

		}
	}

	private Double computeDistance(Shop currShop, Shop shop) {
		double x = currShop.x - shop.x;
		double y = currShop.y - shop.y;
		return Math.sqrt(x * x + y * y);
	}

	public String toString() {
		String s = "";
		for (Shop sh : shops)
			s = s + " " + sh;
		s = s + "\n Distance Matrix \n " + distance;
		return s;
	}

	class Shop {
		int id;
		int x;
		int y;
		int items;
		HashMap<Integer, Integer> prices = new HashMap<Integer, Integer>();

		public String toString() {
			return x + " " + y + " " + prices;
		}
	}

	public static void main(String[] args) throws IOException {
		long start = Calendar.getInstance().getTimeInMillis();
		String resFile = "/Users/alessandro/resultDP.txt";
		FileReader ifi = new FileReader("/Users/alessandro/D-large.in");
		BufferedReader r = new BufferedReader(ifi);
		OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
				resFile));
		BufferedWriter wr = new BufferedWriter(w);
		int n = 0;
		DecimalFormat df = new DecimalFormat("#.0000000");
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		n = Integer.parseInt(r.readLine().trim());
		for (int i = 0; i < n; i++) {
			System.out.println("###### " + i);
			String[] parts = r.readLine().split(" ");
			String items = parts[0];
			String shops = parts[1];
			String gasPrice = parts[2];
			ShoppingPlanDP s = createShoppingPlan(items, shops, gasPrice);
			items = r.readLine();
			s.addItems(items);
			for (int k = 0; k < s.numShops - 1; k++) {
				shops = r.readLine();
				s.addShop(shops);
			}
			s.computeDistanceMatrix();
			double val = s.visit();
			System.out.println(val);
			wr.write("Case #" + (i + 1) + ": " + df.format(val));
			wr.newLine();
		}
		r.close();
		wr.close();
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("Time " + (end - start));
	}

	private static ShoppingPlanDP createShoppingPlan(String items,
			String shops, String gasPrice) {
		ShoppingPlanDP s = new ShoppingPlanDP();
		s.items = Integer.parseInt(items);
		s.numShops = Integer.parseInt(shops) + 1;
		s.gasPrice = Double.parseDouble(gasPrice);
		// 0,0 is a shop with no items
		s.addStart();
		return s;
	}

	private void addStart() {
		Shop s = new Shop();
		s.x = 0;
		s.y = 0;
		shops.add(s);

	}

}


