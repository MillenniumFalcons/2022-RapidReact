package team3647.frc2022.constants;

import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DigitalInput;
import team3647.lib.drivers.LazyTalonFX;

public class TurretConstants {

    public static final TalonFX kTurretMotor = new LazyTalonFX(GlobalConstants.TurretIds.kMotorId);
    public static final TalonFXInvertType kTurretMotorInvert = TalonFXInvertType.CounterClockwise;

    public static final TalonFXConfiguration kMasterConfig = new TalonFXConfiguration();
    public static final double kGearboxReduction = 16 / 60.0 * 16 / 150.0;
    public static final double kFalconPositionToDegrees = kGearboxReduction / 2048.0 * 360;
    public static final double kFalconVelocityToDegpS = kFalconPositionToDegrees * 10;
    public static final double kMaxDegree = 200;
    public static final double kMinDegree = -200;
    public static final double kPosThersholdDeg = 1.0;
    public static final boolean kCurrentLimitingEnable = true;

    public static final double kS = 0.713;
    public static final double kV = 0.0157;
    public static final double kA = 0.000324;

    public static final double kMaxVelocityDegPs = 36;
    public static final double kMaxAccelerationDegPss = 36;
    public static final double kMaxVelocityTicks = kMaxVelocityDegPs / kFalconVelocityToDegpS;
    public static final double kMaxAccelerationTicks =
            kMaxAccelerationDegPss / kFalconVelocityToDegpS;

    public static final double kStallCurrent = 10;
    public static final double kContinuousCurrentLimit = 35; // amps
    public static final double kPeakCurrentLimit = 40; // amps
    public static final double kPeakCurrentDuration = 10; // milliseconds
    public static final double kNominalVoltage = 11;

    public static final Translation2d kRobotToTurretFixed =
            new Translation2d(Units.inchesToMeters(-7), 0);
    public static final Translation2d kTurretToCamTranslationMeters =
            new Translation2d(Units.inchesToMeters(7), 0);
    public static final Pose2d kTurretToCamFixed =
            new Pose2d(kTurretToCamTranslationMeters, new Rotation2d());

    public static final SimpleMotorFeedforward kFeedForwards =
            new SimpleMotorFeedforward(kS, kV, kA);

    public static final DigitalInput kLimitSwitch =
            new DigitalInput(GlobalConstants.TurretIds.kLimitSwitchpin);

    static {
        kMasterConfig.slot0.kP = 0.3;
        kMasterConfig.slot0.kI = 0;
        kMasterConfig.slot0.kD = 0;
        kMasterConfig.slot0.kF = 0;

        kMasterConfig.voltageCompSaturation = kNominalVoltage;
        kMasterConfig.supplyCurrLimit.enable = kCurrentLimitingEnable;
        kMasterConfig.supplyCurrLimit.currentLimit = kStallCurrent;
        kMasterConfig.supplyCurrLimit.triggerThresholdCurrent = kPeakCurrentLimit;
        kMasterConfig.supplyCurrLimit.triggerThresholdTime = kPeakCurrentDuration;

        // in native units/100ms/s
        kMasterConfig.motionAcceleration = kMaxVelocityTicks;
        // in native units/100ms
        kMasterConfig.motionCruiseVelocity = kMaxAccelerationTicks;

        kTurretMotor.configAllSettings(kMasterConfig, GlobalConstants.kTimeoutMS);
        kTurretMotor.setInverted(kTurretMotorInvert);
    }
}
