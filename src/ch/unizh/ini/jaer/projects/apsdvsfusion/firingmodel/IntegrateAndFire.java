/**
 * 
 */
package ch.unizh.ini.jaer.projects.apsdvsfusion.firingmodel;

import java.util.prefs.Preferences;

import ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModel;
import ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModelCreator;
import ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModelMap;

/**
 * @author Dennis Goehlsdorf
 *
 */
public class IntegrateAndFire extends FiringModel {
	float sum = 0.0f;
	float threshold = 1.0f;
	/**
	 * 
	 */
	public IntegrateAndFire(int x, int y, FiringModelMap map) {
		super(x,y,map);
		// TODO Auto-generated constructor stub
	}
	
	public static FiringModelCreator getCreator(Preferences prefs) {
		return new FiringModelCreator("IntegrateAndFire", prefs) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -9075663597897695761L;

			@Override
			public FiringModel createUnit(int x, int y, FiringModelMap map) {
				return new IntegrateAndFire(x,y,map);
			}
		};
	}

	/* (non-Javadoc)
	 * @see ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModel#receiveSpike(double)
	 */
	@Override
	public void receiveSpike(double value, int timeInUs) {
		sum+= value;
		if (sum > threshold) {
			sum = 0.0f;
			emitSpike(1.0, timeInUs);
		}
		else if (sum < 0.0f) {
			sum = 0.0f;
		}
	}

	@Override
	public void reset() {
		sum = 0.0f;
	}


}