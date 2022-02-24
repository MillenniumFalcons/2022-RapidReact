// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package team3647.frc2022.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import team3647.frc2022.subsystems.PivotClimber;

public class LeveledClimberOpenloop extends CommandBase {
    /** Creates a new leveledClimberOpenloop. */
    private final double demand;

    private final PIDController pid;
    private double leftCurrent;
    private double rightCurrent;
    private double leftDemand;
    private double rightDemand;
    private final PivotClimber climber;
    private final double kCurrentThreshold;

    public LeveledClimberOpenloop(
            double demand, double kCurrentThreshold, PivotClimber climber, PIDController pid) {
        this.demand = demand;
        this.kCurrentThreshold = kCurrentThreshold;
        this.climber = climber;
        this.pid = pid;
        addRequirements(climber);
        // Use addRequirements() here to declare subsystem dependencies.
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {}

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        if (demand > 0) {
            climber.setOpenloop(demand);
            return;
        }
        leftCurrent = climber.getLeftCurrent();
        rightCurrent = climber.getRightCurrent();
        if (leftCurrent - kCurrentThreshold > rightCurrent) {
            double percentOutput =
                    MathUtil.clamp(pid.calculate(leftCurrent - rightCurrent, 0), demand, 0);
            leftDemand = demand + percentOutput;
            rightDemand = demand - percentOutput;
            climber.setOpenloop(leftDemand, rightDemand);

        } else if (rightCurrent - kCurrentThreshold > leftCurrent) {
            double percentOutput =
                    MathUtil.clamp(pid.calculate(rightCurrent - leftCurrent, 0), demand, 0);
            leftDemand = demand - percentOutput;
            rightDemand = demand + percentOutput;
            climber.setOpenloop(leftDemand, rightDemand);
        } else {
            climber.setOpenloop(demand);
        }
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
        climber.setOpenloop(0);
    }

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return false;
    }
}
