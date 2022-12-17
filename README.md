# SYSC 3303 Elevator Project - Group 7

**Breakdown of responsibilities**
- Zakaria Damou [Messages, Subsystem, Sequence Diagram, FloorSubsystem]
- Ryan Godfrey [Elevator Subsystem, Elevator, Tests, UML Class Diagram, Elevator State Machine Diagram]
- Rahel Gunaratne [Scheduler, Messages, Tests]
- Sarah Jaber [Scheduler, Tests, UML Class Diagram, Scheduler State Machine Diagram, Scheduler Error Handling]
- Matthew Siu [Messages, Subsystem, Tests, Elevator State Machine Diagram, Elevator Error]

## Project Files:
The Project is broken up into several different packages, which each fulfill a different purpose. The packages include the following:

**common_classes**: [Config.java, Helper.java,Subsystem.java]
- Includes classes that are used across different packages. 

**elevator_subsystem**: [DoorState.java, Elevator.java, ElevatorButton.java, ElevatorSubsystem.java, Instruction.java, MotorState.java, Status.java]
- Includes classes responsible for the Elevator Subsystem functionality. 
- The ElevatorSubsystem handles sending messages and receiving instructions. 

**floor_subsystem**: [FloorSubsystem.java]
- Includes classes responsible for the Floor Subsystem functionality
- The FloorSubsystem.java handles sending requests and receving responses. 

**messages**: [ElevatorMessage.java, FloorRequest.java, Message.java, Register.java, RequestListMessage.java, Response.java, Terminate.java]

- Includes classes representing messages being passed between systems.

**scheduler**: [Scheduler.java, SchedulerMessageReceiver.java]
- Includes classes responsible for the Scheduler subsystem. 
- Includes classes responsible to receive messages.

## Set up instructions:
- You can run the program by running the main() method in the Scheduler.java, FloorSubsystem.java and ElevatorSubsystem.java in that order. 

## Testing instructions:
- You can test the program by running the JUnit tests. The test relies on passengerTest.txt in the Resources folder. The tests checks that the elevator subsystem, floor subsystem, scheduler, work as expected. 
