import java.io.BufferedReader;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Instance { 
	//List<Double> objValues = new ArrayList<Double>();
	int nProd = 4;  
	int nPeriod = Product.per; // t = 10 period number (1 period, 1 day, 12 hours, 720 minutes)
	int nMachine = 3; 
	
	int mt = 810;
	double smax = 18;
	double umax = 9;
	
	
	//outputs of model1
	double[][][] model1_Y = new double[nProd][nMachine][nPeriod]; 
	double[][] model1_Iend = new double[nProd][nPeriod];
	double[][] model1_Uwip = new double[nProd][nPeriod];
	double model1_obj = 0;
	//outputs of model2
	double[][] model2_Iend = new double[nProd][nPeriod];
	double[][] model2_Uwip = new double[nProd][nPeriod];
	double[][] model2_V = new double[nMachine][nPeriod];
	double model2_obj = 0;
	
	//temporary variables which shows previous iterations values		
	double[][][] temp_Y = new double[nProd][nMachine][nPeriod];
	double[][] temp_V = new double[nMachine][nPeriod];
	double temp_obj = 0;
	String temp_res; // problemin çözülebilir olup olmadýðýný kontrol etmek amacýyla yazýlan variable initalize edilmesi falan
	
	//loop and condition to exit
	boolean loop=true;
	boolean condition;
	
	Product[] myProducts; 
	
	public Instance() {} //empty constructor
	
//	public void getList(List<Double> dnm) {
//		
//		for(int i = 0; i<objValues.size();i++) {
//		dnm.add(objValues.get(i));
//		}
//	}
	
	public void ReadFromFile(String fileName) //Reading .csv file.
	{
		String line;
  	  try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"))) {
  		int i =0; //Keeping up with the rows
  	      while ((line = br.readLine()) != null) {
  	    
  	          // Split by a comma separator
  	          String[] split = line.split(";");
  	          // Skipping first row in .csv
  	          if(i==0) {
  	        	  i++; 
  	          continue;
  	          }  
  	          //setting myProducts with corresponding values
  	          myProducts[i-1].setCost(Double.parseDouble(split[1])); 
  	          myProducts[i-1].setPrice(Double.parseDouble(split[2]));
  	          myProducts[i-1].setEndtcost(Double.parseDouble(split[3]));
  	          myProducts[i-1].setWiptcost(Double.parseDouble(split[4]));
  	          myProducts[i-1].setInvtime(Double.parseDouble(split[5]));
//  	      System.out.println( myProducts[i-1].getInvtime());
  	          myProducts[i-1].setTrancost(Double.parseDouble(split[6]));
  	          for(int j = 0; j<nPeriod;j++) {
  	        	myProducts[i-1].setDemand(myProducts,i-1,j,Double.parseDouble(split[7+j]));
  	          }
  	          i++;
  	          }
  	  } catch (Exception e) {
  	      e.printStackTrace();
  	      }
  	  }

	
	public void InitializeProblem(String fileName1) { // Initialize Method which initilaize all values
		
		myProducts = new Product[nProd]; 
		
		for(int t =0 ; t<nPeriod;t++) { //starting values of machines in Model_1
			model2_V[0][t]=50; //	50 olarak en baþtan al
			model2_V[1][t]=26.6;
			model2_V[2][t]=80;
		}
		
		for (int i = 0 ; i < nProd ; i++)  
				myProducts[i] = new Product();
		
		ReadFromFile(fileName1);
			
	}
	
	
	public double totalDemand(int sPeriod,int prodInd) { // Summation of demands from given period to end of the period of given product
		double sum=0;
		for(;sPeriod<nPeriod;sPeriod++) {
			sum=sum+myProducts[prodInd].getDemand(myProducts,prodInd,sPeriod);
		}
		
		return sum;
	}
	
	
}
