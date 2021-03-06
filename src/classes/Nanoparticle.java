package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import util.Configuration;
import util.Constants;

public class Nanoparticle {
	
	public double diameter, radius, ligandlength, x, y, z;
	
	int nn; //number of nearest neighbors
	Nanoparticle[] nearestNeighbors;  //nearest neighbor array
	
	int cn; //number of closest neighbors
	Nanoparticle[] closeNeighbors;  //close neighbor array
	
	int hcn;
	Nanoparticle[] holeCloseNeighbors;  //close neighbor array
	
	int nelectrons; // number of electrons on board
	LinkedList<Electron>[] electronsOnNP;   //use LinkedList here because we will do a lot of electron deletion and insertion.
	
	int nholes; // number of holes on board
	LinkedList<Hole>[] holesOnNP;
	
	HashMap<Nanoparticle, Double> edgeDistanceMap; //distance map to neighbors, edge to edge
	HashMap<Nanoparticle, Double> centerDistanceMap; //distance map to neighbors, center to center

	
	long hotness;
	
	double[] cbenergy = new double[2];
	double cbenergy1, cbenergy2;
	
	double[] vbenergy = new double[2];
	double vbenergy1, vbenergy2;
	
	double dcin;
	
	double selfenergy0, selfenergy;
	double hselfenergy0, hselfenergy;
	
	public boolean inCluster, source, drain;
	int clusterIndex; 
	
	int[] occupationCB, occupationVB, occupationMAX;
	int occupationTotalElectron, occupationTotalHoles;
	//int[] electronIndex, holeIndex;
	
	public Nanoparticle(double xcoord, double ycoord, double zcoord, double diameter_input, Sample sample) {
		// TODO Auto-generated constructor stub
		x = xcoord*Constants.nmtobohr;
		y = ycoord*Constants.nmtobohr;
		z = zcoord*Constants.nmtobohr;
		diameter = diameter_input*Constants.nmtobohr - 2.0*sample.ligandlength;
		radius = diameter/2;
		set_cbenergy();
		set_vbenergy();
		set_occupation(sample);
		if(z-radius <= sample.ndist_thr){
			source = true;
			sample.sources.add(this);
		}
		if(sample.cellz - (z+radius) <= sample.ndist_thr){
			drain = true;
			sample.drains.add(this);
		}
		
		electronsOnNP = new LinkedList[2];
		holesOnNP = new LinkedList[2];
		for(int i=0; i<2; i++){
			electronsOnNP[i] = new LinkedList<Electron>();
			holesOnNP[i] = new LinkedList<Hole>();
		}
		
	}
	
	
	private void set_cbenergy(){
		 // Kand and Wise, d in nm, energy in eV
		 // cbenergy=evtory*(0.238383 +4.28063 *diameter**(-1.87117 ))
		 // cbenergy2=0.254004 +8.0018 *diameter**(-1.82088 )
		 // localdiam=diameter*bohrtonm
         cbenergy1 = Constants.evtory*(-4.238383 + 4.28063*Math.pow((diameter*Constants.bohrtonm), -1.87117));
		 cbenergy[0] = Constants.evtory*(-4.238383 + 4.28063*Math.pow((diameter*Constants.bohrtonm), -1.87117));
		 cbenergy2 = Constants.evtory*(-4.254004 + 8.0018*Math.pow((diameter*Constants.bohrtonm), -1.82088));
	     cbenergy[1] = Constants.evtory*(-4.254004 + 8.0018*Math.pow((diameter*Constants.bohrtonm), -1.82088));
	}
	
	private void set_vbenergy(){
	     // Kand and Wise, d in nm, energy in eV
	     // localdiam=diameter*bohrtonm
	     // vbenergy=evtory*(-0.220257 -5.23931 *diameter**(-1.85753 ))
		 vbenergy1 = Constants.evtory*(-4.728265 - 5.23931*Math.pow((diameter*Constants.bohrtonm), -1.85753));
		 vbenergy[0] = Constants.evtory*(-4.728265 - 5.23931*Math.pow((diameter*Constants.bohrtonm), -1.85753));
	}
	
	private void set_occupation(Sample sample){
		occupationCB = new int[sample.nbnd];
		occupationVB = new int[sample.nbnd];
		occupationMAX = new int[sample.nbnd];
		for(int i=0; i<sample.nbnd; i++){
			occupationMAX[i] = sample.e_degeneracy;  // for symmetric CB and VB only
		}
		occupationTotalElectron = 0;
		occupationTotalHoles = 0;
	}
	
	
	/**
	@param otherNP, sample, if periodic in z, if center to center
	@return NP-NP distance
	@throws what kind of exception does this method throw
	*/
	public double npnpdistance(Nanoparticle otherNP, Sample sample, boolean connected_z, boolean CenterToCenter) {
		
		double deltax, deltay,deltaz, distSquared;
		double cellx = sample.cellx;
		double celly = sample.celly;
		double cellz = sample.cellz;
		
		if(Configuration.PERX)
			deltax = Math.min(Math.min(Math.abs(this.x-otherNP.x), Math.abs(this.x-(otherNP.x-cellx))), Math.abs(this.x-(otherNP.x+cellx)));
		else
			deltax = this.x - otherNP.x ;
		
		if(Configuration.PERY)
			deltay = Math.min(Math.min(Math.abs(this.y-otherNP.y), Math.abs(this.y-(otherNP.y-celly))), Math.abs(this.y-(otherNP.y+celly)));
		else
			deltay = this.y - otherNP.y ;
		
		if(connected_z)
			deltaz = Math.min(Math.min(Math.abs(this.z-otherNP.z), Math.abs(this.z-(otherNP.z-cellz))), Math.abs(this.z-(otherNP.z+cellz)));
		else
			deltaz = this.z - otherNP.z ;
			
		distSquared = Math.pow(deltax, 2.0) + Math.pow(deltay, 2.0) + Math.pow(deltaz, 2.0);

		if(distSquared==0){
			System.out.println("NP-NP overlaps!");
			return 0.0;
		}else{
			if(CenterToCenter)
				return Math.pow(distSquared, 0.5);
			else
				return Math.pow(distSquared, 0.5)-(this.radius + otherNP.radius);
		}
		
	}
	
	private void set_shiftCB(double energyShift){
		cbenergy[0] += energyShift;
	}
	
	private void set_shiftVB (double energyshift){
		vbenergy[0] += energyshift;
	}
	
	public double getCB1() {
		return cbenergy[0];
	}
	
	public int[] getCBoccupation() {
		return occupationCB;
	}
	
	public int[] getMAXocc() {
		return occupationMAX;
	}
	
	// simply add one electron
	public void add_electron(Electron e, int band){
		//System.out.println("adding to "+this);
		//e.hostNP = this;
		electronsOnNP[band].add(e);
		occupationCB[band]++;
		occupationTotalElectron++;
		
	}
	
	// TODO : implement these methods in the Nanoparticle class
	// used for the grand canonical ensemble
	// works for ONE band only
	public boolean add_electron_try(Sample sample) {
		
		double energy_before, energy_after, energy_diff = 0;
		double probability, debug;
		
		for(int band=0; band<sample.nbnd; band++){
			if(occupationCB[band]<occupationMAX[band]){
				

				//energy before insertion, using only one band
				energy_before = totalEnergy(occupationCB[0], occupationVB[0], 0, sample);
				  
				//energy after insertion
				energy_after = totalEnergy(occupationCB[0]+1, occupationVB[0], 0, sample);
				
				// energy difference
				energy_diff = sample.chemicalPotential + energy_before - energy_after ; 
				
				
				
				// TODO: working here now 12_30
				// The numbers are HUGE, does not make any sense, need to correct!!!!!!
				probability = Math.min(1, (sample.V_prime/ sample.nelec)*Math.exp(energy_diff/(Constants.k_boltzmann_ry*sample.temperature)));
				
				debug = Math.exp(energy_diff/Constants.k_boltzmann_ry*sample.temperature);
				//debug = energy_diff/(sample.temperature);
				
				
				System.out.println("adding probability is "+probability);
				
				return true;
			}
		}
		
		
		return false;
		
	}
	
	
	
	// Works for ONE band only !!!!
	private double totalEnergy(int numElectrons, int numHoles, int band, Sample sample) {
		
		double energy = 0;
		
		// total kinetic energy
		//energy += (numElectrons * cbenergy[band] + numHoles * vbenergy[band]);
				
		// external 'horizontal' potential
		energy += 1.0* Constants.sqrt2 * sample.voltage * this.z * numElectrons ;   
		//energy += 1.0* Constants.sqrt2 * sample.voltage * this.z * numHoles ;   

		// onsite charging
		if(numElectrons > 0){
			// add first electron
			energy += (Configuration.lcapacitance0)? selfenergy0 : 0;
			//System.out.println(selfenergy0+" self "+selfenergy);
			// add subsequent electron
			energy += (numElectrons>1) ? (numElectrons)*selfenergy : 0;
		} 
		if(numHoles > 0){
			// add first electron
			energy += (Configuration.lcapacitance0)? hselfenergy0 : 0;
			// add subsequent electron
			energy += (numHoles>1) ? (numHoles)*hselfenergy : 0;
		}
		
		// TODO: ignoring excitonic effect for the moment, as there should be ZERO holes.
		System.out.println("energy is "+energy);
		return energy;
	}
	
	// TODO
	// used for the grand canonical ensemble
	// try to add one electron to the NP. 
	public boolean remove_electron_try(Sample sample) {
		return false;
	}
	
	// simply remove one electron
	public void remove_electron(Electron e, int band){
		//System.out.println("removing from "+e.hostNP+" "+this);
		electronsOnNP[band].remove(e);
		occupationCB[band]--;
		occupationTotalElectron--;
	}
	
	
	public void add_hole(Hole h, int band){
		holesOnNP[band].add(h);
		occupationVB[band]++;
		occupationTotalHoles++;
	}
	
	public void remove_hole(Hole h, int band) {
		holesOnNP[band].remove(h);
		occupationVB[band]--;
		occupationTotalHoles--;
		
	}
	
	
    // update events, single NP version
    public void updateEvents(boolean self, Sample sample) {
    	// update self events
    	//System.out.println("updating NP"+ nanop);
    	if(self){
			for(int band=0; band<sample.nbnd; band++){
				for(Electron e: electronsOnNP[band]){
					// Erase all existing rates on the charge
					sample.rateOnSample -= e.ratesOnCharge;
					// look for new events and update the system total rate
					sample.rateOnSample += e.lookForEvents(sample);
				}
			}
		}
		// update neighboring events
		for(Nanoparticle neighborNP: nearestNeighbors){
			//// only update the ones that has not been updated
			////if(!currentUpdate.contains(neighborNP)){
				for(int band=0; band<sample.nbnd; band++){
					for(Electron e: neighborNP.electronsOnNP[band]){
						// Same as previous
						sample.rateOnSample -= e.ratesOnCharge;
						sample.rateOnSample += e.lookForEvents(sample);
					}
				}
				// add the updated NP to the updated list
				////currentUpdate.add(neighborNP);
			////}
		}
	}
    
	
	
	
	
}
