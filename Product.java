
public class Product 
{
		public final static int per = 30;
		double cost; 
		double[] demand = new double [per]; // a demand for every period
		double price;
		double endtcost; // end item holding cost,,,
		double wiptcost; // work-in-process item holding cost,,,
		double invtime;  // inventory transportation time
		double trancost; // transportation cost
		
		
		
		public Product() {} 
		
		public double getDemand(Product myProduct[],int index, int index2) {
			return myProduct[index].demand[index2];
		}

		public void setDemand(Product myProduct[],int index,int index2,double x) { 
			myProduct[index].demand[index2] = x;
		}

		public double getCost() {
			return cost;
		}

		public double getEndtcost() {
			return endtcost;
		}

		public void setEndtcost(double endtcost) {
			this.endtcost = endtcost;
		}

		public double getWiptcost() {
			return wiptcost;
		}

		public void setWiptcost(double wiptcost) {
			this.wiptcost = wiptcost;
		}

		public double getInvtime() {
			return invtime;
		}

		public void setInvtime(double invtime) {
			this.invtime = invtime;
		}

		public double getTrancost() {
			return trancost;
		}

		public void setTrancost(double trancost) {
			this.trancost = trancost;
		}

		public void setCost(double cost) {
			this.cost = cost;
		}

	

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		@Override
		public String toString() {
			return " This Product's cost is " + cost + " with demand of " + demand + " units and selling price is " + price;
		}
		
		
}
