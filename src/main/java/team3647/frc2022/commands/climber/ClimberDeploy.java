// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package team3647.frc2022.commands.climber;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import team3647.frc2022.commands.TurretMotionMagic;
import team3647.frc2022.constants.ClimberConstants;
import team3647.frc2022.subsystems.PivotClimber;
import team3647.frc2022.subsystems.Turret;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class ClimberDeploy extends SequentialCommandGroup {
    /** Creates a new ClimberDeploy. */
    public ClimberDeploy(PivotClimber climber, Turret turret) {
        // Add your commands in the addCommands() call, e.g.
        // addCommands(new FooCommand(), new BarCommand());
        addCommands(
                new TurretMotionMagic(turret, 0),
                new InstantCommand(climber::setAngled),
                new WaitCommand(0.1),
                new ClimberLength(climber, ClimberConstants.kLengthJustOverLowBar),
                new InstantCommand(climber::setStraight));
    }
}
