package com.algaworks.algasensors.temperature.processing.api.controller;

import com.algaworks.algasensors.temperature.processing.api.model.TemperatureLogOutput;
import com.algaworks.algasensors.temperature.processing.common.IdGenerator;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

import static com.algaworks.algasensors.temperature.processing.infrastructure.rabbitmq.RabbitMQConfig.TEMPERATURE_PROCESSING_FANOUT_EXCHANGE;

@RestController
@RequestMapping("/api/sensors/{sensorId}/temperatures/data")
@Slf4j
@RequiredArgsConstructor
public class TemperatureProcessingController {
	
	private final RabbitTemplate rabbitMQTemplate;
	
	@PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
	public void data(@PathVariable TSID sensorId, @RequestBody String input) {
		if (!isValidInput(input)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		
		double temperature;
		
		try {
			temperature = Double.parseDouble(input);
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
		
		TemperatureLogOutput logOutput = TemperatureLogOutput.builder()
				.id(IdGenerator.generatedTImeBasedUUID())
				.sensorId(sensorId)
				.value(temperature)
				.registeredAt(OffsetDateTime.now())
				.build();
		
		log.info(logOutput.toString());
		
		//Envio da mensagem para o RabbitMQ
		
		String exchange = TEMPERATURE_PROCESSING_FANOUT_EXCHANGE;
		String routingKey = "";
		Object payload = logOutput;
		
		MessagePostProcessor messagePostProcessor = message -> {
			message.getMessageProperties().setHeader("sensorId", logOutput.getSensorId().toString());
			return message;
		};
		
		rabbitMQTemplate.convertAndSend(exchange, routingKey, payload, messagePostProcessor);
	}
	
	private boolean isValidInput(String input) {
		return input != null && !input.isBlank();
	}
}
