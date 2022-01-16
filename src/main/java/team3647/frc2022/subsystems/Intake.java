// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package team3647.frc2022.subsystems;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import edu.wpi.first.wpilibj.Solenoid;
import team3647.lib.TalonFXSubsystem;

/** Add your docs here. */
public class Intake extends TalonFXSubsystem {
    private final Solenoid pistons;
    private boolean extendIntake = false;

    public Intake(
            TalonFX master,
            double velocityConversion,
            double positionConversion,
            Solenoid pistons) {
        super(master, velocityConversion, positionConversion);
        this.pistons = pistons;
    }

    public void extend() {
        extendIntake = true;
    }

    public void retract() {
        extendIntake = false;
    }

    @Override
    public void writePeriodicOutputs() {
        super.writePeriodicOutputs();
        pistons.set(extendIntake);
    }

    @Override
    public String getName() {
        return "Intake";
    }
}
