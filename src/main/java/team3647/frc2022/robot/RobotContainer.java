// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package team3647.frc2022.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import team3647.frc2022.commands.ArcadeDrive;
import team3647.frc2022.subsystems.Drivetrain;
import team3647.lib.inputs.Joysticks;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  // edu.wpi.first.wpilibj.PowerDistribution replaces PDP.java from 3647 lib
  // edu.wpi.first.wpilibj.Compressor replaces Compressor.java from 3647 lib 
  // edu.wpi.first.wpilibj.Solenoid replaces Solenoid.java from 3647 lib
  private final CommandScheduler m_commandScheduler = CommandScheduler.getInstance();

  private final Joysticks mainController = new Joysticks(0);
  private final Joysticks coController = new Joysticks(1);
  
  private final Drivetrain m_drivetrain = new Drivetrain(Constants.CDrivetrain.kLeftMaster, Constants.CDrivetrain.kRightMaster, Constants.CDrivetrain.kLeftSlave, Constants.CDrivetrain.kRightSlave, 
    Constants.CDrivetrain.kPigeonIMU, Constants.CDrivetrain.kFeedforward, Constants.CDrivetrain.kPoseEstimator, 
    Constants.CDrivetrain.kFalconVelocityToMpS, Constants.CDrivetrain.kFalconTicksToMeters, Constants.CDrivetrain.kNominalVoltage);

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the button bindings
    m_commandScheduler.setDefaultCommand(m_drivetrain, new ArcadeDrive(m_drivetrain, mainController::getLeftStickY, mainController::getRightStickX));
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {}

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return null;
  }
}
