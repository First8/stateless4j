package com.github.oxo42.stateless4j;

import org.junit.Assert;
import org.junit.Test;

public class ParallelStateMachineTests {

    @Test
    public void diseaseExample() {

        StateMachineConfig<String, String> config = new StateMachineConfig<>();
        config.enableEntryActionOfInitialState();

        // configure parallel substate "location" of parallel state "person"
        ParallelStateMachineConfig<String, String> locationConfig = new ParallelStateMachineConfig<>("holland", config);
        locationConfig.configure("holland").substateOf("location").permit("migrate", "germany");
        locationConfig.configure("germany").substateOf("location");
        locationConfig.enableEntryActionOfInitialState();
        config.configure("person").parallel(locationConfig);


        ParallelStateMachineConfig<String, String> biologyConfig = new ParallelStateMachineConfig<>("alive", config);
        biologyConfig.enableEntryActionOfInitialState();
        config.configure("person").parallel(biologyConfig);

        // configure substates of parallel substate "biology"
        biologyConfig.configure("alive").substateOf("biology");
        biologyConfig.configure("dead").substateOf("biology");

        // configure parallel substates of parallel state "alive"
        ParallelStateMachineConfig<String, String> diseaseConfig = new ParallelStateMachineConfig<>("susceptible", biologyConfig);
        diseaseConfig.enableEntryActionOfInitialState();
        biologyConfig.configure("alive").parallel(diseaseConfig);

        ParallelStateMachineConfig<String, String> treatmentsConfig = new ParallelStateMachineConfig<>("treatments", biologyConfig);
        treatmentsConfig.enableEntryActionOfInitialState();
        biologyConfig.configure("alive").parallel(treatmentsConfig);

        // configure substates of parallel state "disease"
        diseaseConfig.configure("susceptible").substateOf("disease").permit("infect", "infected");
        diseaseConfig.configure("infected").substateOf("disease").permit("recover", "recovered");
        diseaseConfig.configure("recovered").substateOf("disease");

        StateMachine<String, String> machine = new StateMachine<>("person", config);

        StateMachineState<String> machineState = machine.getStateMachineState();

        machine.fire("infect");

        machineState = machine.getStateMachineState();

        Assert.assertTrue(machineState.isInState("infected"));
    }
}
