package com.algaworks.algasensors.temperature.processing.common;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;

import java.util.UUID;

public class IdGenerator {
	
	private static TimeBasedEpochRandomGenerator timeBasedEpochRandomGenerator =
			Generators.timeBasedEpochRandomGenerator();
	
	private IdGenerator() {
	}
	
	public static UUID generatedTImeBasedUUID() {
		return timeBasedEpochRandomGenerator.generate();
	}
}