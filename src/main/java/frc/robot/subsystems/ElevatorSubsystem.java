package frc.robot.subsystems;

import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.Command;

public class ElevatorSubsystem extends SubsystemBase {

    final TrapezoidProfile.Constraints motionConstraints = new TrapezoidProfile.Constraints(0.25, 0.125);
    final TrapezoidProfile motionProfile = new TrapezoidProfile(motionConstraints);
    final double maxHeight = 1.5; // Meters
    final double minHeight = 0;

    double currUserSetPoint = 0;
    double currProfileSetPoint = 0;
    double currPredictedVelocity = 0;
    boolean m_isActive;

    public ElevatorSubsystem() {
        m_isActive = false;

        initializePositions();
    }

    private void initializePositions() {

        if (RobotBase.isSimulation()) {
            currUserSetPoint = 0;
            currProfileSetPoint = 0;
            currPredictedVelocity = 0;
        }
    }

    public double getUserSetPoint() {
        return currUserSetPoint;
    }

    public void setUserSetPoint(double userSetPoint) {

        if (userSetPoint > maxHeight) {

            currUserSetPoint = maxHeight;
        } else if (userSetPoint < minHeight) {
            userSetPoint = minHeight;
        } else {
            currUserSetPoint = userSetPoint;
        }

    }

    /*
     * Returns the setpoint used by the motion profile. Only use this if you know
     * that you want it.
     * 
     */
    public double getProfileSetPoint() {
        return currProfileSetPoint;
    }

    public boolean isActive() {
        return m_isActive;
    }

    public void activate() {
        m_isActive = true;
    }

    public void deactivate() {
        m_isActive = false;
    }
    
    public void periodic() {

        final double dT = 1. / 20;

        if (m_isActive) {
            TrapezoidProfile.State currState = new TrapezoidProfile.State(currProfileSetPoint, currPredictedVelocity);
            TrapezoidProfile.State goalState = new TrapezoidProfile.State(currUserSetPoint, 0);
            TrapezoidProfile.State nextState = motionProfile.calculate(dT, currState, goalState);
            currPredictedVelocity = nextState.velocity;
            currProfileSetPoint = nextState.position;
        }
    }

    // A test command to verify the system moves

    private void pingPongFunction() {
        final double span = maxHeight - minHeight;
        if (m_isActive) {
            if ((getProfileSetPoint() - minHeight) < 0.05 * span) {

                setUserSetPoint(maxHeight);
            } else if ((maxHeight - getProfileSetPoint()) < 0.05 * span) {
                setUserSetPoint(minHeight);
            }
        }
    }

    public Command pingPongCommand() {

        return this.run(() -> pingPongFunction());
    }

}