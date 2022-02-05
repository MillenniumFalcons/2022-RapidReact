// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package team3647.frc2022.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import team3647.frc2022.subsystems.ColumnBottom;
import team3647.frc2022.subsystems.ColumnTop;
import team3647.frc2022.subsystems.Flywheel;

public class ShootBall extends CommandBase {
    private final Flywheel flywheel;
    private final ColumnTop columnTop;
    private final ColumnBottom columnBottom;
    private final double surfaceVel;

    public ShootBall(
            Flywheel flywheel, ColumnTop columnTop, ColumnBottom columnBottom, double surfaceVel) {
        this.flywheel = flywheel;
        this.columnTop = columnTop;
        this.columnBottom = columnBottom;
        this.surfaceVel = surfaceVel;
        addRequirements(flywheel, columnTop, columnBottom);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {}

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        flywheel.setOpenloop(0.5);
        // columnTop.setSurfaceVelocity(surfaceVel * 0.8);
        if (flywheel.getVelocity() > surfaceVel * 0.9) {
            columnBottom.setOpenloop(0.5);
        }
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
        flywheel.setOpenloop(0);
        // columnTop.setOpenloop(0);
        columnBottom.setOpenloop(0);
    }

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return false;
    }
}
