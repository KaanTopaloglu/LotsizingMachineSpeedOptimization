import ilog.concert.*;
import ilog.cplex.*;
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.image.Image;
//import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;



public class Test{
	
	List<Double> objValues  = new ArrayList<Double>();
	
	
	//public List<Double> objValues;

	
	public static void main(String[] args) {

		//objValues = new ArrayList<Double>();
	Test test = new Test();
	
	
	
	Instance instance = new Instance(); 
	instance.InitializeProblem("Input1.csv");
	boolean denm = false;
	int i = 1;
	double epsilone = 0.05;
	
	long startTime = System.currentTimeMillis();
	System.out.println(startTime);
	while(instance.loop) { //solution of the non-linearity in the model with two-phase method
		
		instance.temp_res=""; // üretim rate'i azaltma condition'unun 0'la
		
		//saving previous iteration's value in temp_ variables
		for(int k= 0 ; k < instance.nProd; k++) 				
				for(int t = 0; t < instance.nPeriod; t++ )  	
					for(int m = 0; m < instance.nMachine; m++ )
						instance.temp_Y[k][m][t]=instance.model1_Y[k][m][t];
		
		test.Model1(instance); //new iteration's values
		
		//saving previous iteration's value in temp_ variables
		instance.temp_obj=instance.model2_obj;
		
		
		for(int m = 0 ; m < instance.nMachine ; m++)
			for(int t = 0 ; t < instance.nPeriod ; t++ )
				instance.temp_V[m][t]=instance.model2_V[m][t];
		
		test.Model2(instance); // new iteration's values
		
		for(int k= 0 ; k < instance.nProd; k++) {		
			for(int t = 0; t < instance.nPeriod; t++ ) { 					
				for(int m = 0; m < instance.nMachine; m++ ) {
					if(instance.temp_Y[k][m][t]==instance.model1_Y[k][m][t]) 
						instance.condition = true; 
					else {
						instance.condition = false; 
						break;
					}	
				}
				if(instance.condition!=true) 
					break;
			}
			if(instance.condition!=true) 
				break;
		}
		
		if(instance.condition==true) {
			for(int m = 0 ; m < instance.nMachine ; m++) { 
				for(int t = 0 ; t < instance.nPeriod ; t++ ) {
					if(instance.temp_V[m][t]==instance.model2_V[m][t]) 
						instance.condition = true;
					else {
						instance.condition = false; 
						break;
					}
				}
				if(instance.condition!=true)
					break;
			}
		}
		
		if(instance.condition==true) {
			for(int k = 0 ; k < instance.nProd ; k++) {
				for(int t = 0 ; t < instance.nPeriod ; t++ ) {
					if(instance.model1_Iend[k][t]==instance.model2_Iend[k][t])
						instance.condition = true;
					else {
						instance.condition = false;
						break;
					}
				}
				if(instance.condition!=true)
					break;
			}
		}
		
		if(instance.condition==true) {
			for(int k = 0 ; k < instance.nProd ; k++) {
				for(int t = 0 ; t < instance.nPeriod ; t++ ) {
					if(instance.model1_Uwip[k][t]==instance.model2_Uwip[k][t])
						instance.condition = true;
					else {
						instance.condition = false;
						break;
					}
				}
				if(instance.condition!=true)
					break;
			}
			
		}
		
		
		System.out.println("  ");
		System.out.println("Iteration number : "+ i);
		System.out.println("  ");
		i++;
		
		double a=0;
		double b=0;
		double c=0;
		
		
		for(int t = 0 ; t < instance.nPeriod ; t++ ) {
			for(int k = 0 ; k < instance.nProd ; k++) {
				a = instance.model1_Uwip[k][t]-instance.model2_Uwip[k][t];
				b = instance.model1_Iend[k][t]-instance.model2_Iend[k][t];
				c = instance.model1_obj - instance.model2_obj;
				if( (a < epsilone) || ( b <epsilone) ||  (c  < epsilone)) {
					denm=true;
					break;
				}
			}
			if(denm==true)
				break;
		}
		
		if(instance.condition && denm && !instance.temp_res.equals("problem not solved")) {
			instance.loop=false;
			break;
		}
		else if(instance.temp_obj!=instance.model2_obj || instance.model1_obj!=instance.model2_obj) //if model's objective function is different, continue to next iteration
			continue;
		else if(instance.condition!=true) //if any of the condition is not satisfied, condition is false, continue to next iteration
			continue;
		else if(instance.temp_res.equals("problem not solved")) { //Eðer üretim rate'i çok yavaþsa ve bundan dolayý demand'ler satisfy'lanmýyorsa yani "problem not solved ise":
			for(int t = 0; t<instance.nPeriod;t++) { //baþlangýç hýz deðerlerini 0.5 dakika düþür ve iterasyona en baþtan baþla
				if(instance.model2_V[0][t]>50)
					instance.model2_V[0][t]=instance.model2_V[0][t]-0.5;
				continue;
			}	
			continue;
		}
		else // if objective's are same and every condition is satisfied (all decision variables are same)
			instance.loop=false; // set loop to false, exit the while loop.

	}  //while loop end	
	
	long stopTime = System.currentTimeMillis();
	long elapsedTime = stopTime - startTime;
	System.out.println("Elapsed time was " + elapsedTime + " miliseconds.");
	
	//Writes a text file to see final values
	

	
//	test.Model1(instance);
//	test.Model2(instance); 
	
	
		try {
			Writer output;
			output = new BufferedWriter(new FileWriter("Final_output.txt"));  
			output.write(" OBJ IS FOUND AS : "+instance.model2_obj+ ", TOTAL ITERATION NUMBER IS : "+ i +", ELAPSED TIME IS :"+ elapsedTime +"\n");
			if(stopTime-startTime>=30000)
				output.write("TIME LIMIT EXCEEDED \n");
			for(int t = 0; t < instance.nPeriod; t++) {
				for(int m = 0; m < instance.nMachine; m++ ) {
					for( int k= 0 ; k < instance.nProd; k++) {	
						output.write("Y(" + k + "," + m + "," + t + ") is : "+instance.model1_Y[k][m][t] + "               " 
					+ "V(" + m + "," + t + ") is : " + instance.model2_V[m][t] + "               " 
					+ "Iend(" + k + "," + t + ") is : " + instance.model1_Iend[k][t] + "               " 
					+ "Uwip(" + k + "," + t + ") is : " + instance.model1_Uwip[k][t] + "\n");
						}
					output.write("\n");
					}
				output.write("------------------------------------------------------------------------------------------------------------- \n");
				}
    	      output.close();
    	      }
		
	 catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	      }
////			---------------------------------------------------------------------
		try {
			Writer output;
			output = new BufferedWriter(new FileWriter("Total production.txt"));  
			for(int t = 0; t < instance.nPeriod; t++) {
					
				output.write((instance.model1_Y[0][0][t]+(instance.model1_Y[1][2][t])+instance.model1_Y[2][1][t]+(instance.model1_Y[3][2][t]))+ " ");
				output.write("\n");
				}
    	      output.close();
    	      }
		
	 catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	      }
		
		
//	--------------------------------------------------------------------------------------------------------------------------
try {
	Writer output;
	output = new BufferedWriter(new FileWriter("Y_Amounts.txt"));  
		for(int t = 0; t < instance.nPeriod; t++) {
			output.write(instance.model1_Y[0][0][t]+ " ");
			output.write("\n");
		}
		output.write("-------------------------------------------------------------------------------------------------------------\n");
		for(int a = 0; a < instance.nPeriod; a++) {
			output.write(instance.model1_Y[1][2][a] + " ");
			output.write("\n");
		}
		output.write("-------------------------------------------------------------------------------------------------------------\n");
		for(int t = 0; t < instance.nPeriod; t++) {
			output.write(instance.model1_Y[2][1][t] + " ");
			output.write("\n");
		}
		output.write("-------------------------------------------------------------------------------------------------------------\n");
		for(int t = 0; t < instance.nPeriod; t++) {
			output.write(instance.model1_Y[3][2][t] + " ");
			output.write("\n");
		}
      output.close();
      }

catch (IOException e) {
  System.out.println("An error occurred.");
  e.printStackTrace();
  }
		
//		---------------------------------------------------------------------
	try {
		Writer output;
		output = new BufferedWriter(new FileWriter("PL1andCut_Amounts.txt"));  
		output.write("-----------------------------PL1 PRODUCTION-------------------------------------- \n");
		for(int t = 0; t < instance.nPeriod; t++) {
				output.write(instance.model1_Y[1][0][t] + " ");
				output.write("\n");
			}
		output.write("-----------------------------CUT PRODUCTION-------------------------------------- \n");
		for(int t = 0; t < instance.nPeriod; t++) {
			output.write(instance.model1_Y[1][2][t] + " ");
			output.write("\n");
		}
		output.write("-----------------------------WIP AMOUNTS---------------------------------------- \n");
		for(int t = 0; t < instance.nPeriod; t++) {
			output.write(instance.model1_Uwip[1][t] + " ");
			output.write("\n");
		}
	      output.close();
	      }
	
 catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
      }
		
//	---------------------------------------------------------------------
try {
	Writer output;
	output = new BufferedWriter(new FileWriter("PL2andCut_Amounts.txt"));  
	output.write("-----------------------------PL2 PRODUCTION-------------------------------------- \n");
	for(int t = 0; t < instance.nPeriod; t++) {
			output.write(instance.model1_Y[3][1][t] + " ");
			output.write("\n");
		}
	output.write("-----------------------------CUT PRODUCTION---------------------------------------- \n");
	for(int t = 0; t < instance.nPeriod; t++) {
		output.write(instance.model1_Y[3][2][t] + " ");
		output.write("\n");
	}
	output.write("-----------------------------WIP AMOUNTS---------------------------------------- \n");
	for(int t = 0; t < instance.nPeriod; t++) {
		output.write(instance.model1_Uwip[3][t] + " ");
		output.write("\n");
	}
      output.close();
      }

catch (IOException e) {
  System.out.println("An error occurred.");
  e.printStackTrace();
  }
		
	
//		---------------------------------------------------------------------
	try {
		Writer output;
		output = new BufferedWriter(new FileWriter("tIEND_Amounts.txt"));  
		for(int t = 0; t < instance.nPeriod; t++) {
			double a = 0;
				for( int k= 0 ; k < instance.nProd; k++) {			
					a = a+ instance.model1_Iend[k][t];
				}
				output.write(a + " ");
				output.write("\n");
			}
	      output.close();
	      }
	
 catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
      }
//	---------------------------------------------------------------------
try {
	Writer output;
	output = new BufferedWriter(new FileWriter("UWIP_Amounts.txt"));  
	for(int t = 0; t < instance.nPeriod; t++) {
		double a = 0;
			for( int k= 0 ; k < instance.nProd; k++) {			
				a = a+ instance.model1_Uwip[k][t];
			}
			output.write(a + " ");
			output.write("\n");
		}
      output.close();
      }

catch (IOException e) {
  System.out.println("An error occurred.");
  e.printStackTrace();
  }
	
	//---------------------------------------------------------------------
	try {
		Writer output;
		output = new BufferedWriter(new FileWriter("V(0)_Rates.txt"));  
		for(int t = 0; t < instance.nPeriod; t++) {
			output.write(instance.model2_V[0][t]+" ");
			output.write("\n");

		}
	output.close();
	}
	catch (IOException e) {
		System.out.println("An error occurred.");
		e.printStackTrace();
	}
	
	//---------------------------------------------------------------------
		try {
			Writer output;
			output = new BufferedWriter(new FileWriter("V(1)_Rates.txt"));  
			for(int t = 0; t < instance.nPeriod; t++) {
				output.write(instance.model2_V[1][t]+" ");
				output.write("\n");

			}
		output.close();
		}

		catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
		//---------------------------------------------------------------------
				try {
					Writer output;
					output = new BufferedWriter(new FileWriter("V(2)_Rates.txt"));  
					for(int t = 0; t < instance.nPeriod; t++) {
						output.write(instance.model2_V[2][t]+" ");
						output.write("\n");

					}
				output.close();
				}

				catch (IOException e) {
					System.out.println("An error occurred.");
					e.printStackTrace();
				}
		

		
		
	}  // END OF THE MAIN METHOD
	
	
	/* -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ---------------------------------------------------------- MODEL 1 SECTION --------------------------------------------------------------------------------
	 * -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	
	
	
	public void Model1(Instance instance) { // Model1, maximizes profit with given production rates of the machines
		
		try {
			//initializing decision variables of Model1
			IloCplex cplex = new IloCplex();
			IloNumVar[][] dnm_V = new IloNumVar[instance.nMachine][];
			IloNumVar[][][] Y = new IloNumVar[instance.nProd][instance.nMachine][];
			IloNumVar[][] Iend = new IloNumVar[instance.nProd][];
			IloNumVar[][] Uwip = new IloNumVar[instance.nProd][];  
			
			for (int k = 0; k < instance.nProd; k++){
		        Y[k] = new IloNumVar[instance.nMachine][];
		        	Iend[k] =cplex.numVarArray(instance.nPeriod,0, instance.smax); //max end inventory of given product within given period 
		        	Uwip[k] =cplex.numVarArray(instance.nPeriod,0, instance.umax); //max wip inventory of given product within given period
		        for (int m = 0; m < instance.nMachine; m++){
		        	dnm_V[m] = cplex.numVarArray(instance.nPeriod,1,1);
		        	Y[k][m] = cplex.numVarArray(instance.nPeriod, 0, Double.MAX_VALUE); //Goes to + infinity
		        	//
		        	for(int t=0; t< instance.nPeriod;t++) {
		    		   Y[k][m][t].setName("Y(" + k + "," + m + "," + t + ")"); 
		    		   Iend[k][t].setName("Iend(" + k + "," + t + ")");
		    		   Uwip[k][t].setName("Uwip(" + k + "," + t + ")");
		    	   }
		        }     
		     }//883094.52---885146.52
		
	    IloLinearNumExpr objective = cplex.linearNumExpr(); // Linear expression of Objective Function
	    
	    for(int t=0; t<instance.nPeriod;t++) {
	    	for(int m=0; m<instance.nMachine;m++) {
	    		for(int k=0; k < instance.nProd;k++) { 
	    			
	    			if(k==1 && m==2) {
//	    				objective.addTerm(instance.myProducts[k].getPrice(),Y[k][m][t]); //total selling price 
	    				objective.addTerm(instance.myProducts[k].getCost(),Y[k][m][t]);//production cost (max profit olurken negatif deðer olmalý)
	    			}
	    			if(k==2 && m==1) {
//	    				objective.addTerm(instance.myProducts[k].getPrice(),Y[k][m][t]); //total selling price 
		    			objective.addTerm(instance.myProducts[k].getCost(),Y[k][m][t]);//production cost (max profit olurken negatif deðer olmalý)
		    		}
	    			if(k==3 && m==2) {
//		    			objective.addTerm(instance.myProducts[k].getPrice(),Y[k][m][t]); //total selling price 
		    			objective.addTerm(instance.myProducts[k].getCost(),Y[k][m][t]);//production cost (max profit olurken negatif deðer olmalý)
		    		}
	    			if(k==0 && m==0) {
//		    			objective.addTerm(instance.myProducts[k].getPrice(),Y[k][m][t]); //total selling price 
		    			objective.addTerm(instance.myProducts[k].getCost(),Y[k][m][t]);//production cost (max profit olurken negatif deðer olmalý)
		    		}
	    			
	    			if(m==0) { // Uwip and Iend must run only once since they don't bound to nMachine (max profit olurken negatif deðer olmalý)
	    			objective.addTerm(instance.myProducts[k].getEndtcost(),Iend[k][t]);  //end item holding cost
	    			objective.addTerm(instance.myProducts[k].getWiptcost(),Uwip[k][t]);  //wip item holding cost 
	    			objective.addTerm(instance.myProducts[k].getInvtime()*instance.myProducts[k].getTrancost(),Uwip[k][t]); //wip item transportation cost
	    			objective.addTerm(instance.myProducts[k].getInvtime()*instance.myProducts[k].getTrancost(),Iend[k][t]); // end item transportation cost
	    			objective.addTerm(-1.16*instance.model2_V[0][t],dnm_V[0][t]);
	    			objective.addTerm(-3.09*instance.model2_V[1][t],dnm_V[1][t]);
	    			}
	    		}
	    	}
	    }
	        
	    
		    //production flow root constraints
	    for(int t=0; t<instance.nPeriod;t++) {

	    	IloLinearNumExpr cons1  = cplex.linearNumExpr();
		    IloLinearNumExpr cons2  = cplex.linearNumExpr();
		    IloLinearNumExpr cons3  = cplex.linearNumExpr();
		    IloLinearNumExpr cons4  = cplex.linearNumExpr();
	    	
	    	cons1.addTerm(1,Y[2][0][t]);
	    	cons2.addTerm(1,Y[2][1][t]);

	    		cplex.addEq(cons1,cons2); 
	    	
	    	cons3.addTerm(1,Y[3][0][t]);
	    	cons4.addTerm(1,Y[3][1][t]);
	    	
	    		cplex.addEq(cons3,cons4);
	
	    	
	    	for(int m=0; m<instance.nMachine;m++) {
	    		for(int k=0; k < instance.nProd;k++) {
	    			if(m==0)continue;
	    			switch(k) {
	    			case 0:
	    				cplex.addEq(Y[k][m][t],0);
	    				continue;
	    			case 1:if(m==k) {
	    				cplex.addEq(Y[k][m][t],0);
	    				continue;
	    				}
	    			case 2:if(m==k) {
	    				cplex.addEq(Y[k][m][t],0);
	    				continue;
	    				}
	    			}
	    		}
	    	}
	    }

	    
		for (int t = 0; t< instance.nPeriod; t++) { // end item balance constraint
	    	if(t==0) { //Starting end item inventory is 0. 
	    		cplex.addEq(cplex.sum(0,Y[0][0][t]),cplex.sum(instance.myProducts[0].getDemand(instance.myProducts,0,t),Iend[0][t]));
	    		cplex.addEq(cplex.sum(0,Y[1][2][t]),cplex.sum(instance.myProducts[1].getDemand(instance.myProducts,1,t),Iend[1][t]));
	    		cplex.addEq(cplex.sum(0,Y[2][1][t]),cplex.sum(instance.myProducts[2].getDemand(instance.myProducts,2,t),Iend[2][t]));
	    		cplex.addEq(cplex.sum(0,Y[3][2][t]),cplex.sum(instance.myProducts[3].getDemand(instance.myProducts,3,t),Iend[3][t]));
	    		continue;
	    	}
	    	
	    	cplex.addEq(cplex.sum(Iend[0][t-1],Y[0][0][t]),cplex.sum(instance.myProducts[0].getDemand(instance.myProducts,0,t),Iend[0][t]));
	    	cplex.addEq(cplex.sum(Iend[1][t-1],Y[1][2][t]),cplex.sum(instance.myProducts[1].getDemand(instance.myProducts,1,t),Iend[1][t]));
	    	cplex.addEq(cplex.sum(Iend[2][t-1],Y[2][1][t]),cplex.sum(instance.myProducts[2].getDemand(instance.myProducts,2,t),Iend[2][t]));
	    	cplex.addEq(cplex.sum(Iend[3][t-1],Y[3][2][t]),cplex.sum(instance.myProducts[3].getDemand(instance.myProducts,3,t),Iend[3][t]));

	    }
		
		for(int t = 0 ; t < instance.nPeriod ; t++) {
			if (t == 0) {
				cplex.addEq(Y[1][0][t],cplex.sum(Y[1][2][t],Uwip[1][t]));
				cplex.addEq(Y[3][1][t],cplex.sum(Y[3][2][t],Uwip[3][t]));
				continue;
			}
			cplex.addEq(cplex.sum(Y[1][0][t],Uwip[1][t-1]),cplex.sum(Y[1][2][t],Uwip[1][t]));
			cplex.addEq(cplex.sum(Y[3][1][t],Uwip[3][t-1]),cplex.sum(Y[3][2][t],Uwip[3][t]));	
		}

		 
		for(int t = 0 ; t< instance.nPeriod;t++) 
		{ // well it equality 
			IloLinearNumExpr conswip  = cplex.linearNumExpr();
			conswip.addTerm(80,Y[1][0][t]);
			conswip.addTerm(80,Y[3][1][t]);
			conswip.addTerm(-80,Uwip[1][t]);
			conswip.addTerm(-80,Uwip[3][t]);
			// WIP OLUÞMUYOR!!!! WIP YERINE END ITEM BIRAKIYOR, ÖNCEDEN ÜRETÝYOR, WIP OLUÞURMUYOR, PERIOD 8: NONCHEM PLAK=6, CHEM PLAK=9, IEND OLARAK GÖSTERÝYOR
			cplex.addLe(conswip,instance.mt);
		}
		
		double[] kind= new double[4]; //total demand
        for(int t =0;t<instance.nPeriod;t++) {
            for(int k = 0; k< instance.nProd;k++) {
            kind[k]=kind[k]+instance.myProducts[k].getDemand(instance.myProducts,k,t);  
            }  
        }
        

		 IloLinearNumExpr condemand0  = cplex.linearNumExpr();
	     IloLinearNumExpr condemand1  = cplex.linearNumExpr();
	     IloLinearNumExpr condemand2  = cplex.linearNumExpr();
	     IloLinearNumExpr condemand3  = cplex.linearNumExpr();
	     IloLinearNumExpr condemand4  = cplex.linearNumExpr();
	     IloLinearNumExpr condemand5  = cplex.linearNumExpr();
	     IloLinearNumExpr condemand6  = cplex.linearNumExpr();
	     IloLinearNumExpr condemand7  = cplex.linearNumExpr();
	     
	     
	     for(int k= 0 ; k < instance.nProd; k++) { 
				for(int m = 0; m < instance.nMachine; m++ ) {
					for(int t = 0; t < instance.nPeriod; t++ ) {
						
						if(k==0 && m==0) {
							condemand0.addTerm(1,Y[k][m][t]);
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand0,kind[k]);
						}
						else if(k==1 && m==0) {
							condemand1.addTerm(1,Y[k][m][t]); 
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand1,kind[k]);
							}
						else if(k==1 && m==2) {
							condemand4.addTerm(1,Y[k][m][t]);
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand4,kind[k]);
							}
						else if(k==2 && m==0) {
							condemand2.addTerm(1,Y[k][m][t]);
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand2,kind[k]);
						}
						else if(k==2 && m==1) {
							condemand5.addTerm(1,Y[k][m][t]);
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand5,kind[k]);
						}
						else if(k==3 && m==0) {
							condemand3.addTerm(1,Y[k][m][t]);
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand3,kind[k]);
						}
						else if(k==3 && m==1) {
							condemand6.addTerm(1,Y[k][m][t]);
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand6,kind[k]);
						}
						else if(k==3 && m==2) {
							condemand7.addTerm(1,Y[k][m][t]);
							if(t==instance.nPeriod-1)
								cplex.addLe(condemand7,kind[k]);
						}
					}
				}
			}
	     
	     
	    
	    for(int t = 0;t<instance.nPeriod;t++) 
				cplex.addEq(Uwip[0][t],0);
			
	     
	    for(int k= 0 ; k < instance.nProd; k++) 
			for(int m = 0; m < instance.nMachine; m++ ) 
				for(int t = 0; t < instance.nPeriod; t++ ) 
					cplex.addLe(Y[k][m][t],instance.totalDemand(t,k));
			
	   
	    
	    for(int t = 0; t< instance.nPeriod;t++) { // END ITEM INVENTORY LIMIT WAREHOUSE
	    	cplex.addLe(cplex.sum(cplex.sum(Iend[0][t],Iend[1][t]),cplex.sum(Iend[2][t],Iend[3][t])),instance.smax);
	    }
	    
	    for(int t = 0; t< instance.nPeriod;t++) { // END ITEM INVENTORY LIMIT WAREHOUSE
	    	cplex.addLe(cplex.sum(cplex.sum(Uwip[0][t],Uwip[1][t]),cplex.sum(Uwip[2][t],Uwip[3][t])),instance.umax);
	    }
	    /*
	    WIP ÝÇÝN DE AYNISINI YAP 
	    */
	    
	    
	    for(int t=0; t<instance.nPeriod;t++) {
	    	IloLinearNumExpr concapPL1  = cplex.linearNumExpr();
	 	    IloLinearNumExpr concapPL2  = cplex.linearNumExpr();
	 	    IloLinearNumExpr concapCUT  = cplex.linearNumExpr();
	    	for(int m=0; m<instance.nMachine;m++) { 
	    		for(int k=0; k < instance.nProd;k++) { 
	    			if(m==0) {
	    				concapPL1.addTerm(instance.model2_V[m][t],Y[k][m][t]);
	    			}	
	    			else if(m==1) { 
	    				concapPL2.addTerm(instance.model2_V[m][t],Y[k][m][t]);
	    			}
	    			else if(m==2) { 
	    				concapCUT.addTerm(instance.model2_V[m][t],Y[k][m][t]);
	    			}	
	    		}
	    	}
	    	cplex.addLe(concapPL1,instance.mt);
	    	cplex.addLe(concapPL2,instance.mt);
	    	cplex.addLe(concapCUT,instance.mt);
	    }
	   
	    	cplex.addMinimize(objective);
	    	if (cplex.solve()) {
        		System.out.println("obj = "+cplex.getObjValue());
        		cplex.getStatus();
        		instance.model1_obj=cplex.getObjValue();
        		
        		for(int k= 0 ; k < instance.nProd; k++) {
        			for(int m = 0; m < instance.nMachine; m++ ) { 
        				for(int t = 0; t < instance.nPeriod; t++ ) {
        					instance.model1_Y[k][m][t] = cplex.getValue(Y[k][m][t]);
        					if(m==0) {
        		
        						instance.model1_Iend[k][t] = cplex.getValue(Iend[k][t]);
        						instance.model1_Uwip[k][t] = cplex.getValue(Uwip[k][t]);
        					}
        				}
        			}
        		}
        		try {
        			Writer output;
        			output = new BufferedWriter(new FileWriter("Final_output_model1.txt"));  
        			output.write(" OBJ IS FOUND AS : "+instance.model2_obj+"\n");
        			for(int t = 0; t < instance.nPeriod; t++) {
        				for(int m = 0; m < instance.nMachine; m++ ) {
        					for( int k= 0 ; k < instance.nProd; k++) {	
        						output.write("Y(" + k + "," + m + "," + t + ") is : "+instance.model1_Y[k][m][t] + "               " 
        					+ "V(" + m + "," + t + ") is : " + instance.model2_V[m][t] + "               " 
        					+ "Iend(" + k + "," + t + ") is : " + instance.model1_Iend[k][t] + "               " 
        					+ "Uwip(" + k + "," + t + ") is : " + instance.model1_Uwip[k][t] + "\n");
        						}
        					output.write("\n");
        					}
        				output.write("-----------------------------------------------------------------------------------------------------------------------------"+ "\n");
        				}
            	      output.close();
            	      }
        		
        	 catch (IOException e) {
        	      System.out.println("An error occurred.");
        	      e.printStackTrace();
        	      }
        			
        		
        		
	    	}// if bitiþ
	    	
        	else {
        		
        		System.out.println("problem not solved");
        		instance.temp_res="problem not solved";
        	}
	    	
	    	cplex.exportModel("Model1.lp");
	    	cplex.writeSolution("model1_solution.sol"); 
        	cplex.end();
	} 
	catch(IloException exc) {
		exc.printStackTrace();
		}
	}
	
	/* -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ---------------------------------------------------------- MODEL 2 SECTION --------------------------------------------------------------------------------
	 * -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * -----------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	
	
	public void Model2(Instance instance) { // Model2, maximizes profit with given production amount of the machines

		try {
			
			IloCplex cplex = new IloCplex();
			IloNumVar[][] V = new IloNumVar[instance.nMachine][];
			IloNumVar[][][] dnm = new IloNumVar[instance.nProd][instance.nMachine][];
			IloNumVar[][] Iend = new IloNumVar[instance.nProd][];
			IloNumVar[][] Uwip = new IloNumVar[instance.nProd][];
			
			     for (int k = 0; k < instance.nProd; k++){
			    	 dnm[k] = new IloNumVar[instance.nMachine][];
			    	 
				        	Iend[k] =cplex.numVarArray(instance.nPeriod,0, instance.smax); //max end inventory of given product within given period 
				        	Uwip[k] =cplex.numVarArray(instance.nPeriod,0, instance.umax); //max wip inventory of given product within given period
				 
			      for (int m = 0; m < instance.nMachine; m++){
			    	  dnm[k][m] = cplex.numVarArray(instance.nPeriod, 1, 1); // Dummy-ish decision variable whose value is one and helps to add revenue to objective function
			           V[m] = cplex.numVarArray(instance.nPeriod, 0, Double.MAX_VALUE);
			           
			    	   for(int t=0; t< instance.nPeriod;t++) {
			    		   
			    		   V[m][t].setName("V(" + m + "," + t + ")"); 	
			    		   Iend[k][t].setName("Iend(" + k + "," + t + ")");
			    		   Uwip[k][t].setName("Uwip(" + k + "," + t + ")");
			    	   }
			        }     
			     } 
			
		    IloLinearNumExpr objective = cplex.linearNumExpr();
		    
		    for(int t=0; t<instance.nPeriod;t++) {
		    	for(int m=0; m<instance.nMachine;m++) {
		    		for(int k=0; k < instance.nProd;k++) { 
		    			
		    			if(k==0 && m == 0) {
//		    				objective.addTerm(instance.myProducts[k].getPrice()*instance.model1_Y[k][m][t],dnm[k][m][t]);
			    			objective.addTerm(instance.myProducts[k].getCost()*instance.model1_Y[k][m][t],dnm[k][m][t]);
		    			}
		    			if(k==1 && m == 2) {
//		    				objective.addTerm(instance.myProducts[k].getPrice()*instance.model1_Y[k][m][t],dnm[k][m][t]);
			    			objective.addTerm(instance.myProducts[k].getCost()*instance.model1_Y[k][m][t],dnm[k][m][t]);
		    			}
		    			if(k==2 && m == 1) {
//		    				objective.addTerm(instance.myProducts[k].getPrice()*instance.model1_Y[k][m][t],dnm[k][m][t]);
			    			objective.addTerm(instance.myProducts[k].getCost()*instance.model1_Y[k][m][t],dnm[k][m][t]);
		    			}
		    			if(k==3 && m == 2) {
//		    				objective.addTerm(instance.myProducts[k].getPrice()*instance.model1_Y[k][m][t],dnm[k][m][t]);
			    			objective.addTerm(instance.myProducts[k].getCost()*instance.model1_Y[k][m][t],dnm[k][m][t]);
		    			}
		    			
		    			if(m==0) {
		    			objective.addTerm(instance.myProducts[k].getEndtcost(),Iend[k][t]);    //end item holding cost
		    			objective.addTerm(instance.myProducts[k].getWiptcost(),Uwip[k][t]);    //wip item holding cost
		    			objective.addTerm(instance.myProducts[k].getInvtime()*instance.myProducts[k].getTrancost(),Uwip[k][t]); //wip item transportation cost
		    			objective.addTerm(instance.myProducts[k].getInvtime()*instance.myProducts[k].getTrancost(),Iend[k][t]);
		    			objective.addTerm(-1.16,V[0][t]);
		    			objective.addTerm(-3.09,V[1][t]);
		    			}
		    		}
		    	}
		    }
		
		   
		    
			for (int t = 0; t< instance.nPeriod; t++) { // end item balance constraint
		    	if(t==0) { 

		    		cplex.addEq(instance.model1_Y[0][0][t],cplex.sum(instance.myProducts[0].getDemand(instance.myProducts,0,t),Iend[0][t]));
		    		cplex.addEq(instance.model1_Y[1][2][t],cplex.sum(instance.myProducts[1].getDemand(instance.myProducts,1,t),Iend[1][t]));
		    		cplex.addEq(instance.model1_Y[2][1][t],cplex.sum(instance.myProducts[2].getDemand(instance.myProducts,2,t),Iend[2][t]));
		    		cplex.addEq(instance.model1_Y[3][2][t],cplex.sum(instance.myProducts[3].getDemand(instance.myProducts,3,t),Iend[3][t]));
		    		continue; 
		    	}
		    	
		    	cplex.addEq(cplex.sum(Iend[0][t-1],instance.model1_Y[0][0][t]),cplex.sum(instance.myProducts[0].getDemand(instance.myProducts,0,t),Iend[0][t]));
		    	cplex.addEq(cplex.sum(Iend[1][t-1],instance.model1_Y[1][2][t]),cplex.sum(instance.myProducts[1].getDemand(instance.myProducts,1,t),Iend[1][t]));
		    	cplex.addEq(cplex.sum(Iend[2][t-1],instance.model1_Y[2][1][t]),cplex.sum(instance.myProducts[2].getDemand(instance.myProducts,2,t),Iend[2][t]));
		    	cplex.addEq(cplex.sum(Iend[3][t-1],instance.model1_Y[3][2][t]),cplex.sum(instance.myProducts[3].getDemand(instance.myProducts,3,t),Iend[3][t]));
		    }
			
			
			for(int t = 0 ; t < instance.nPeriod ; t++) {
				if (t == 0) {
					cplex.addEq(instance.model1_Y[1][0][t],cplex.sum(instance.model1_Y[1][2][t],Uwip[1][t]));
					cplex.addEq(instance.model1_Y[3][1][t],cplex.sum(instance.model1_Y[3][2][t],Uwip[3][t]));
					continue;
				}
				cplex.addEq(cplex.sum(instance.model1_Y[1][0][t],Uwip[1][t-1]),cplex.sum(instance.model1_Y[1][2][t],Uwip[1][t]));
				cplex.addEq(cplex.sum(instance.model1_Y[3][1][t],Uwip[3][t-1]),cplex.sum(instance.model1_Y[3][2][t],Uwip[3][t]));	
			}	
		
			for(int t = 0 ; t< instance.nPeriod;t++)
			{
				IloLinearNumExpr conswip  = cplex.linearNumExpr();
				conswip.addTerm(V[2][t],instance.model1_Y[1][0][t]);
				conswip.addTerm(V[2][t],instance.model1_Y[3][1][t]);
				conswip.addTerm(-80,Uwip[1][t]);
				conswip.addTerm(-80,Uwip[3][t]);
					
				
				cplex.addLe(conswip,instance.mt);
				
			}
			
			
			//production rate bounds of the machines
			for(int t = 0 ; t<instance.nPeriod;t++) {
				 for(int m = 0; m<instance.nMachine;m++) {
					 switch(m) {
					 case 0:
						 V[m][t].setUB(80);
						 V[m][t].setLB(50);
						 continue;
					 case 1:
						 V[m][t].setUB(26.6);
						 V[m][t].setLB(22);
						 continue;
					 case 2:
						cplex.addEq(V[m][t],80);
					 }
				 }
			 }
			
			for(int t = 0;t<instance.nPeriod;t++)  //MAX ALLOWED WIP IN THAT PERIOD
				cplex.addEq(Uwip[0][t],0);
			
			
			for(int t = 0; t< instance.nPeriod;t++) { //MAX TOTAL ALLOWED END ITEM INVENTORY IN THAT PERIOD
		    	cplex.addLe(cplex.sum(cplex.sum(Iend[0][t],Iend[1][t]),cplex.sum(Iend[2][t],Iend[3][t])),50);
		    }
			
		    for(int t=0; t<instance.nPeriod;t++) {
		    	if(t==0) {
		    		cplex.addEq(instance.model1_Y[2][0][t],cplex.sum(instance.model1_Y[2][1][t],Uwip[2][t]));
		    		cplex.addEq(instance.model1_Y[3][0][t],cplex.sum(instance.model1_Y[3][1][t],Uwip[2][t]));
		    		continue;
		    		}
		    	cplex.addEq(cplex.sum(Uwip[2][t-1],instance.model1_Y[2][0][t]),cplex.sum(instance.model1_Y[2][1][t],Uwip[2][t]));
		    	cplex.addEq(cplex.sum(Uwip[2][t-1],instance.model1_Y[3][0][t]),cplex.sum(instance.model1_Y[3][1][t],Uwip[2][t]));	
		    	
		    }	
		    
		    for(int t=0; t<instance.nPeriod;t++) {
		    	IloLinearNumExpr concapPL1  = cplex.linearNumExpr();
		 	    IloLinearNumExpr concapPL2  = cplex.linearNumExpr();
		 	    IloLinearNumExpr concapCUT  = cplex.linearNumExpr();
		    	for(int m=0; m<instance.nMachine;m++) { 
		    		for(int k=0; k < instance.nProd;k++) { 
		    			if(m==0) {
		    				concapPL1.addTerm(V[m][t],instance.model1_Y[k][m][t]);
		    			}	
		    			else if(m==1) { 
		    				concapPL2.addTerm(V[m][t],instance.model1_Y[k][m][t]);
		    			}
		    			else if(m==2) { 
		    				concapCUT.addTerm(V[m][t],instance.model1_Y[k][m][t]);
		    			}	
		    		}
		    	}
		    	cplex.addLe(concapPL1,instance.mt); // 1 period 1 gün olsun 1 günde de 12 saat olsun 12*60=720
		    	cplex.addLe(concapPL2,instance.mt);
		    	cplex.addLe(concapCUT,instance.mt);
		    }
		   
		    for(int t = 0; t< instance.nPeriod;t++) { // END ITEM INVENTORY LIMIT WAREHOUSE
		    	cplex.addLe(cplex.sum(cplex.sum(Iend[0][t],Iend[1][t]),cplex.sum(Iend[2][t],Iend[3][t])),instance.smax);
		    }
		    
		    for(int t = 0; t< instance.nPeriod;t++) { // END ITEM INVENTORY LIMIT WAREHOUSE
		    	cplex.addLe(cplex.sum(cplex.sum(Uwip[0][t],Uwip[1][t]),cplex.sum(Uwip[2][t],Uwip[3][t])),instance.umax);
		    }
		    
		    	cplex.addMinimize(objective);
		    	if (cplex.solve()) {
	        		System.out.println("obj = "+cplex.getObjValue());
	        		cplex.getStatus();
	        		
	        	
	        		instance.model2_obj=cplex.getObjValue();
	        		
	        		
	        			for(int m = 0; m < instance.nMachine; m++ ) { 
	        				for(int t = 0; t < instance.nPeriod; t++ ) {
	        					instance.model2_V[m][t] = (double)cplex.getValue(V[m][t]);
	        					for(int k= 0 ; k < instance.nProd; k++) {
	        					if(m==0) {
	        						instance.model2_Iend[k][t] = cplex.getValue(Iend[k][t]);
	        						instance.model2_Uwip[k][t] = cplex.getValue(Uwip[k][t]);
	        					}
	        				}
	        			}
	        		} 	
	        			
	        			try {
	            			Writer output;
	            			output = new BufferedWriter(new FileWriter("Final_output_model2.txt"));  
	            			output.write(" OBJ IS FOUND AS : "+instance.model2_obj+"\n");
	            			for(int t = 0; t < instance.nPeriod; t++) {
	            				for(int m = 0; m < instance.nMachine; m++ ) {
	            					for( int k= 0 ; k < instance.nProd; k++) {	
	            						output.write("Y(" + k + "," + m + "," + t + ") is : "+instance.model1_Y[k][m][t] + "               " 
	            					+ "V(" + m + "," + t + ") is : " + instance.model2_V[m][t] + "               " 
	            					+ "Iend(" + k + "," + t + ") is : " + instance.model2_Iend[k][t] + "               " 
	            					+ "Uwip(" + k + "," + t + ") is : " + instance.model2_Uwip[k][t] + "\n");
	            						}
	            					output.write("\n");
	            					}
	            				output.write("-----------------------------------------------------------------------------------------------------------------------------" + "\n");
	            				}
	                	      output.close();
	                	      }
	            		
	            	 catch (IOException e) {
	            	      System.out.println("An error occurred.");
	            	      e.printStackTrace();
	            	      }
		    	}
		    	
	        	else {
	        		System.out.println("problem not solved");
	        		instance.temp_res="problem not solved";
	        	}
		  
		    	cplex.exportModel("Model2.lp");
	        	cplex.end();
		} 
		catch(IloException exc) {
			exc.printStackTrace();
			}
		}

	

}