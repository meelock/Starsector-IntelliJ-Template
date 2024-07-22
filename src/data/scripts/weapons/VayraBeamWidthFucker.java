//By Nicke535, this script allows a beam weapon to "oscillate" beam width over time
package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.FastTrig;

import java.util.HashMap;
import java.util.Map;

public class VayraBeamWidthFucker implements EveryFrameWeaponEffectPlugin {


    private final float oscillationTimePrim = 0.1f;
    private final float oscillationTimeSec = 0.2f;

    //Instantiates variables we will use later
    private float counter = 0f;
    private boolean runOnce = true;
    private final Map<Integer, BeamAPI> beamMap = new HashMap<>();
    private final Map<Integer, Float> oscillationWidthMap = new HashMap<>();

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run if we are paused, or our if weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }

        //Resets the maps and variables if we are not firing
        if (weapon.getChargeLevel() <= 0) {
            counter = 0f;
            beamMap.clear();
            oscillationWidthMap.clear();
            runOnce = true;
            return;
        }

        //If we are firing, start the code and change variables
        if (weapon.getChargeLevel() > 0f && runOnce) {
            int counterForBeams = 0;
            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getWeapon() == weapon) {
                    if (!beamMap.containsValue(beam)) {
                        beamMap.put(counterForBeams, beam);
                        counterForBeams++;
                    }
                }
            }

            if (!beamMap.isEmpty()) {
                runOnce = false;
            }
        }

        //Advances our time counter
        counter += amount;

        //Get the beams, and flicker in sync.
        for (Integer i : beamMap.keySet()) {
            BeamAPI beam = beamMap.get(i);

            //Instantiates the width map, if necessary
            if (oscillationWidthMap.get(i) == null) {
                oscillationWidthMap.put(i, beam.getWidth());
            }

            //Figures out which oscillation phase the beam should be in: has two parts, with different oscillation frequencies
            float radCountPrim = counter * 2f * (float) Math.PI / oscillationTimePrim;
            float radCountSec = counter * 2f * (float) Math.PI / oscillationTimeSec;
            float oscillationPhasePrim = ((float) FastTrig.sin(radCountPrim) * 0.4f) + 0.6f;
            float oscillationPhaseSec = ((float) FastTrig.sin(radCountSec) * 0.2f) + 0.8f;

            //Then, we calculate how the visuals of the beam should be modified, depending on oscillation phase
            float visMult = oscillationPhasePrim * oscillationPhaseSec;

            //Finally, modifies our beam depending on the visual multiplier
            beam.setWidth(oscillationWidthMap.get(i) * visMult);
        }
    }
}
