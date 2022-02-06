package team3647.frc2022.constants;

import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DigitalInput;
import team3647.lib.drivers.LazyTalonFX;

public final class ColumnTopConstants {
    public static final TalonFXInvertType kMasterInverted = TalonFXInvertType.Clockwise;
    public static final TalonFXConfiguration kMasterConfig = new TalonFXConfiguration();
    public static final double kS = 0.91307;
    public static final double kV = 4.7359;
    public static final double kA = 0.10506;
    public static final SimpleMotorFeedforward kFeedForward =
            new SimpleMotorFeedforward(kS, kV, kA);

    public static final double kNominalVoltage = 12;
    public static final double kGearboxReduction = 1 / 2.0;

    public static final double kWheelDiameterMeters = Units.inchesToMeters(2);
    public static final double kWheelRotationMeters = kWheelDiameterMeters * Math.PI;

    public static final double kPosConverstion =
            kWheelRotationMeters / GlobalConstants.kFalconTicksPerRotation * kGearboxReduction;
    public static final double kNativeVelToSurfaceMpS =
            10 * kWheelDiameterMeters / GlobalConstants.kFalconTicksPerRotation * kGearboxReduction;

    public static final TalonFX kColumnMotor =
            new LazyTalonFX(GlobalConstants.ColumnTopIds.kMotorId);
    public static final DigitalInput kTopBanner =
            new DigitalInput(GlobalConstants.ColumnTopIds.kTopLimitSwitchPin);

    static {
        kMasterConfig.slot0.kP = 0.0014586;
        kMasterConfig.slot0.kI = 0;
        kMasterConfig.slot0.kD = 0;
        kMasterConfig.slot0.kF = 0;

        kMasterConfig.voltageCompSaturation = kNominalVoltage;

        kColumnMotor.configAllSettings(kMasterConfig, GlobalConstants.kTimeoutMS);
    }
}
