package team3647.frc2022.subsystems;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandGroupBase;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.ScheduleCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import team3647.frc2022.commands.ClimberCommands;
import team3647.frc2022.commands.ColumnTopCommands;
import team3647.frc2022.commands.FeederCommands;
import team3647.frc2022.commands.FlywheelCommands;
import team3647.frc2022.commands.HoodCommands;
import team3647.frc2022.commands.IntakeCommands;
import team3647.frc2022.commands.turret.AimTurret;
import team3647.frc2022.commands.turret.TurretCommands;
import team3647.frc2022.constants.ColumnTopConstants;
import team3647.frc2022.constants.FlywheelConstants;
import team3647.frc2022.constants.HoodContants;
import team3647.frc2022.constants.LEDConstants;
import team3647.frc2022.states.ClimberState;
import team3647.frc2022.states.RobotState;
import team3647.frc2022.states.ShooterState;
import team3647.frc2022.states.TurretState;
import team3647.lib.tracking.FlightDeck;
import team3647.lib.vision.AimingParameters;

public class Superstructure {
    public RobotState currentState = new RobotState();

    private AimingParameters aimingParameters;
    private double flywheelVelocity = 0;
    private double kickerVelocity = 0;
    private double hoodAngle = 16;

    public Superstructure(
            FlightDeck deck,
            PivotClimber m_climber,
            ColumnBottom m_columnBottom,
            ColumnTop m_columnTop,
            Intake m_intake,
            Turret m_turret,
            Hood m_hood,
            Flywheel m_flywheel,
            Ballstopper ballstopper,
            Compressor compressor,
            StatusLED statusLEDs,
            BooleanSupplier drivetrainStopped) {
        this.deck = deck;
        this.m_climber = m_climber;
        this.m_columnBottom = m_columnBottom;
        this.m_columnTop = m_columnTop;
        this.m_intake = m_intake;
        this.m_turret = m_turret;
        this.m_hood = m_hood;
        this.m_flywheel = m_flywheel;
        this.m_ballstopper = ballstopper;
        this.compressor = compressor;
        this.m_statusLED = statusLEDs;
        this.drivetrainStopped = drivetrainStopped;

        flywheelCommands = new FlywheelCommands(m_flywheel);
        hoodCommands = new HoodCommands(m_hood);
        climberCommands = new ClimberCommands(m_climber);
        columnTopCommands = new ColumnTopCommands(m_columnTop);
        feederCommands = new FeederCommands(m_columnBottom, m_columnTop, m_ballstopper);
        intakeCommands = new IntakeCommands(m_intake);
        turretCommands = new TurretCommands(m_turret);
        isClimbing = new Trigger(this::isClimbing);
        hasTargetTrigger = new Trigger(this::hasTarget);
        flywheelOnlyReady = new Trigger(() -> getFlywheelReady(this::getAimedFlywheelSurfaceVel));
        newTargetTrigger = new Trigger(this::hasNewTarget);
        isShooting = new Trigger(this::isShooting);
        isAiming = new Trigger(this::isAiming);
        fullyReadyToShoot = new Trigger(this::readyToAutoShoot);
    }

    public Command autoAccelerateAndShoot() {
        return autoAccelerateAndShoot(1.2, 0.4, 0);
    }

    public Command autoAccelerateAndShoot(
            double feederSpeed, double delayBetweenShots, double timeoutAfterDrivetrainStops) {
        return accelerateAndShoot(
                this::getAimedFlywheelSurfaceVel,
                this::getAimedKickerVelocity,
                this::readyToAutoShoot,
                this::autoShootBallWentThrough,
                feederSpeed,
                delayBetweenShots,
                timeoutAfterDrivetrainStops);
    }

    public Command autoAccelerateAndShoot(double feederSpeed) {
        return accelerateAndShoot(
                this::getAimedFlywheelSurfaceVel,
                this::getAimedKickerVelocity,
                this::readyToAutoShoot,
                this::autoShootBallWentThrough,
                feederSpeed,
                0.4,
                0);
    }

    public Command batterAccelerateAndShoot() {
        return new WaitUntilCommand(() -> Math.abs(m_turret.getAngle() + 180) < 3)
                .andThen(
                        accelerateAndShoot(
                                this::getBatterVelocity,
                                () -> ColumnTopConstants.kBatterVelocity,
                                this::readyToBatter,
                                this::batterBallWentThrough,
                                2,
                                0.5,
                                0));
    }

    public Command lowAccelerateAndShoot() {
        return new WaitUntilCommand(() -> Math.abs(m_turret.getAngle() - 0) < 3)
                .andThen(
                        new WaitCommand(0.9),
                        feederCommands.retractStopper(),
                        feederCommands.feedIn(() -> 2))
                .alongWith(
                        flywheelCommands.variableVelocity(() -> FlywheelConstants.kLowGoalVelocity),
                        columnTopCommands.getGoVariableVelocity(
                                () -> ColumnTopConstants.kLowGoalVelocity));
    }

    public Command accelerateAndShoot(
            DoubleSupplier flywhelVelocity,
            DoubleSupplier kickerVelocity,
            BooleanSupplier readyToShoot,
            BooleanSupplier ballWentThrough,
            double feederSpeed,
            double delayBetweenShots,
            double delayAfterDrivetrainStops) {

        return CommandGroupBase.parallel(
                new InstantCommand(() -> currentState.shooterState = ShooterState.SHOOT),
                flywheelCommands.variableVelocity(flywhelVelocity),
                columnTopCommands.getGoVariableVelocity(kickerVelocity),
                CommandGroupBase.sequence(
                        new ConditionalCommand(
                                new InstantCommand(),
                                new WaitUntilCommand(drivetrainStopped)
                                        .andThen(new WaitCommand(delayAfterDrivetrainStops)),
                                drivetrainStopped),
                        CommandGroupBase.sequence(
                                new WaitUntilCommand(readyToShoot),
                                feederCommands.retractStopper(),
                                // Shoot (The second command stops when the first
                                // command ends)
                                feederCommands.feedIn(() -> feederSpeed).until(ballWentThrough),
                                feederCommands.extendStopper(),
                                new WaitCommand(delayBetweenShots),
                                new WaitUntilCommand(readyToShoot),
                                feederCommands.retractStopper(),
                                // Shoot (The second command stops when the first
                                // command ends)
                                feederCommands.feedIn(() -> feederSpeed).until(ballWentThrough),
                                feederCommands.extendStopper(),
                                new WaitCommand(delayBetweenShots),
                                new WaitUntilCommand(readyToShoot),
                                feederCommands.retractStopper(),
                                // Shoot (The second command stops when the first
                                // command ends)
                                feederCommands.feedIn(() -> feederSpeed).until(ballWentThrough),
                                feederCommands.extendStopper())));
    }

    public Command autoClimbSequnce() {
        return new ConditionalCommand(
                        climberCommands.toNextRung(),
                        CommandGroupBase.sequence(
                                new InstantCommand(
                                        () -> {
                                            currentState.climberState = ClimberState.CLIMB;
                                            currentState.turretState = TurretState.HOLD_POSITION;
                                        }),
                                turretCommands.motionMagic(0, 2),
                                climberCommands.deploy()),
                        this::isClimbing)
                .alongWith(new ScheduleCommand(flywheelCommands.stop()));
    }

    public Command extendClimberIfClimbing() {
        return new ConditionalCommand(
                climberCommands.setAngled(), new InstantCommand(), this::isClimbing);
    }

    public Command retractClimberIfClimbing() {
        return new ConditionalCommand(
                climberCommands.setStraight(), new InstantCommand(), this::isClimbing);
    }

    public Command aimTurret() {
        return new ConditionalCommand(
                new InstantCommand(),
                new InstantCommand(() -> this.currentState.turretState = TurretState.AIM)
                        .andThen(
                                new AimTurret(
                                        m_turret,
                                        this::getAimingParameters,
                                        deck.getTracker()::getMeasuredVelocity)),
                this::isClimbing);
    }

    public Command climberManualControl(DoubleSupplier percentOut) {
        return new ConditionalCommand(
                climberCommands.openLoopControl(percentOut),
                new InstantCommand(),
                this::isClimbing);
    }

    public Command deployAndRunIntake(DoubleSupplier surfaceVelocity) {
        return intakeCommands.deploy().andThen(intakeCommands.runClosedLoop(surfaceVelocity));
    }

    public Command runFeeder(DoubleSupplier surfaceVelocity) {
        return feederCommands.extendStopper().andThen(feederCommands.feedIn(surfaceVelocity));
    }

    public Command intakeInThenManual(DoubleSupplier manual) {
        return intakeCommands
                .openLoopAndStop(0.3)
                .withTimeout(0.5)
                .andThen(
                        new RunCommand(
                                () -> {
                                    var leftY = manual.getAsDouble();
                                    m_intake.setOpenloop(leftY * leftY * leftY);
                                },
                                m_intake));
    }

    public Command feederInThenManual(DoubleSupplier manual) {
        return feederCommands
                .feedIn(() -> 2.5)
                .withTimeout(0.5)
                .andThen(
                        new RunCommand(
                                () -> {
                                    var leftY = manual.getAsDouble();
                                    m_columnBottom.setOpenloop(leftY * leftY * leftY * 0.5);
                                },
                                m_columnBottom));
    }

    public Command clearFeederFlywheel() {
        return CommandGroupBase.parallel(
                feederCommands.retractStopper(),
                feederCommands.runColumnBottomOut(),
                columnTopCommands.getRunOutwards(),
                flywheelCommands.openloop(-0.6));
    }

    public Command accelerateWithMinMaxDistance(double minDistance, double maxDistance) {
        return flywheelCommands.variableVelocity(
                () -> this.getAimedFlywhelAtMinMaxDistance(minDistance, maxDistance));
    }

    public Command disableCompressor() {
        return new InstantCommand(compressor::disable);
    }

    public Command enableCompressor() {
        return new InstantCommand(compressor::enableDigital);
    }

    public void periodic(double timestamp) {
        aimingParameters = deck.getLatestParameters();
        if (aimingParameters != null) {
            flywheelVelocity = FlywheelConstants.getFlywheelRPM(aimingParameters.getRangeMeters());
            kickerVelocity = MathUtil.clamp(flywheelVelocity * 0.5, 0, 10);
            hoodAngle = HoodContants.getHoodAngle1(aimingParameters.getRangeMeters());
        }

        if (ClimberState.CLIMB == currentState.climberState) {
            hoodAngle = 15;
        }
    }

    public boolean hasNewTarget() {
        var aimingParams = getAimingParameters();
        if (aimingParams == null) {
            return false;
        }
        return (Timer.getFPGATimestamp() - aimingParams.getLastSeenTimestamp()) < 5;
    }

    public double getDistanceToTarget() {
        if (aimingParameters == null) {
            return 0;
        }
        return aimingParameters.getRangeMeters();
    }

    public boolean readyToShoot(
            DoubleSupplier flywheel, DoubleSupplier kicker, DoubleSupplier hood) {
        return getFlywheelReady(flywheel)
                && Math.abs(m_columnTop.getVelocity() - kicker.getAsDouble()) < 0.3
                && Math.abs(m_hood.getAngle() - hood.getAsDouble()) < 0.1
                && Math.abs(m_flywheel.getVelocity()) > 5
                && Math.abs(m_columnTop.getVelocity()) > 2;
    }

    public boolean readyToLowGoal() {
        return readyToShoot(
                () -> FlywheelConstants.kLowGoalVelocity,
                () -> ColumnTopConstants.kLowGoalVelocity,
                () -> HoodContants.kLowGoalAngle);
    }

    public boolean readyToBatter() {
        return getFlywheelReady(this::getBatterVelocity, 2)
                && Math.abs(m_columnTop.getVelocity() - ColumnTopConstants.kBatterVelocity) < 2
                && Math.abs(m_hood.getAngle() - HoodContants.kBatterAngle) < 1
                && Math.abs(m_flywheel.getVelocity()) > 5
                && Math.abs(m_columnTop.getVelocity()) > 1;
    }

    public boolean readyToAutoShoot() {
        return readyToShoot(
                this::getAimedFlywheelSurfaceVel,
                this::getAimedKickerVelocity,
                this::getAimedHoodAngle);
    }

    public boolean ballWentThrough(
            DoubleSupplier flywheel, DoubleSupplier kicker, double threshold) {
        return m_flywheel.getVelocity() + threshold < flywheel.getAsDouble();
    }

    public boolean lowBallWentThrough() {
        return ballWentThrough(
                () -> FlywheelConstants.kLowGoalVelocity,
                () -> ColumnTopConstants.kLowGoalVelocity,
                0.1);
    }

    public boolean batterBallWentThrough() {
        return ballWentThrough(
                this::getBatterVelocity, () -> ColumnTopConstants.kBatterVelocity, 1);
    }

    public boolean autoShootBallWentThrough() {
        return Math.abs(m_flywheel.getVelocity() / getAimedFlywheelSurfaceVel()) <= 0.91;
    }

    public boolean getFlywheelReady(DoubleSupplier expectedVelocity, double threshold) {
        return Math.abs(m_flywheel.getVelocity() - expectedVelocity.getAsDouble()) < threshold;
    }

    public boolean getFlywheelReady(DoubleSupplier expectedVelocity) {
        return getFlywheelReady(expectedVelocity, 0.1);
    }

    public AimingParameters getAimingParameters() {
        return aimingParameters;
    }

    public double getAimedFlywhelAtMinMaxDistance(double minDistance, double maxDistance) {
        return FlywheelConstants.getFlywheelRPM(
                MathUtil.clamp(getDistanceToTarget(), minDistance, maxDistance));
    }

    public double getBatterVelocity() {
        return FlywheelConstants.kBatterVelocity + getShooterSpeedOffset();
    }

    public double getAimedFlywheelSurfaceVel() {
        return flywheelVelocity;
    }

    public double getAimedKickerVelocity() {
        return kickerVelocity;
    }

    public double getAimedHoodAngle() {
        return hoodAngle;
    }

    public RobotState getRobotState() {
        return currentState;
    }

    public boolean isShooting() {
        return currentState.shooterState == ShooterState.SHOOT;
    }

    public boolean isAiming() {
        return currentState.turretState == TurretState.AIM;
    }

    public boolean isClimbing() {
        return currentState.climberState == ClimberState.CLIMB;
    }

    public boolean hasTarget() {
        return getAimingParameters() != null;
    }

    public double getShooterSpeedOffset() {
        return SmartDashboard.getNumber("Shooter Speed Offset", 0.0);
    }

    public void configLEDTriggers() {
        var newTargetShooting =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger)
                        .and(fullyReadyToShoot)
                        .and(flywheelOnlyReady)
                        .and(isAiming);
        var newTargetReady =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger)
                        .and(fullyReadyToShoot.negate())
                        .and(flywheelOnlyReady)
                        .and(isAiming);
        var newTargetNotReady =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger)
                        .and(fullyReadyToShoot.negate())
                        .and(flywheelOnlyReady.negate())
                        .and(isAiming.negate());
        var newTargetNotReadyAiming =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger)
                        .and(fullyReadyToShoot.negate())
                        .and(flywheelOnlyReady.negate())
                        .and(isAiming);

        var oldTargetShooting =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger.negate())
                        .and(fullyReadyToShoot)
                        .and(flywheelOnlyReady)
                        .and(isAiming);
        var oldTargetReady =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger.negate())
                        .and(fullyReadyToShoot.negate())
                        .and(flywheelOnlyReady)
                        .and(isAiming);
        var oldTargetNotReadyAiming =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger.negate())
                        .and(fullyReadyToShoot.negate())
                        .and(flywheelOnlyReady.negate())
                        .and(isAiming);
        var oldTargetNotReady =
                hasTargetTrigger
                        .and(isClimbing.negate())
                        .and(newTargetTrigger.negate())
                        .and(fullyReadyToShoot.negate())
                        .and(flywheelOnlyReady.negate())
                        .and(isAiming.negate());
        hasTargetTrigger
                .negate()
                .whenActive(() -> m_statusLED.setAnimation(LEDConstants.kBlinkRed), m_statusLED);

        newTargetNotReady.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kSolidWhite), m_statusLED);
        newTargetNotReadyAiming.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kBlinkWhiteFast), m_statusLED);
        newTargetReady.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kBlinkGreen), m_statusLED);
        newTargetShooting.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kSolidkGreen), m_statusLED);

        oldTargetNotReady.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kSolidYellow), m_statusLED);
        oldTargetNotReadyAiming.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kBlinkYellow), m_statusLED);
        oldTargetReady.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kBlinkOrange), m_statusLED);
        oldTargetShooting.whenActive(
                () -> m_statusLED.setAnimation(LEDConstants.kSolidOrange), m_statusLED);

        isClimbing.whenActive(() -> m_statusLED.setAnimation(LEDConstants.kSolidPink), m_statusLED);
    }

    private final FlightDeck deck;
    private final PivotClimber m_climber;
    private final ColumnBottom m_columnBottom;
    private final ColumnTop m_columnTop;
    private final Intake m_intake;
    private final Turret m_turret;
    private final Hood m_hood;
    private final Flywheel m_flywheel;
    private final Ballstopper m_ballstopper;
    private final Compressor compressor;
    private final StatusLED m_statusLED;
    private final BooleanSupplier drivetrainStopped;

    public final FlywheelCommands flywheelCommands;
    public final HoodCommands hoodCommands;
    public final ClimberCommands climberCommands;
    public final TurretCommands turretCommands;
    public final ColumnTopCommands columnTopCommands;
    public final FeederCommands feederCommands;
    public final IntakeCommands intakeCommands;

    public final Trigger flywheelOnlyReady;
    public final Trigger newTargetTrigger;
    public final Trigger hasTargetTrigger;
    public final Trigger isShooting;
    public final Trigger isClimbing;
    public final Trigger fullyReadyToShoot;
    public final Trigger isAiming;
}
