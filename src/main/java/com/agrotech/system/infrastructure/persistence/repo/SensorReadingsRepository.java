package com.agrotech.system.infrastructure.persistence.repo;

import com.agrotech.system.infrastructure.persistence.entity.SensorReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorReadingsRepository extends JpaRepository<SensorReadings, UUID> {
	Optional<SensorReadings> findTopBySensor_IdOrderByRecordedAtDescCreatedAtDesc(UUID sensorId);
	List<SensorReadings> findAllBySensor_IdOrderByRecordedAtDesc(UUID sensorId);
	List<SensorReadings> findAllBySensor_IdAndRecordedAtBetweenOrderByRecordedAtDesc(
			UUID sensorId,
			Instant startDate,
			Instant endDate
	);
}

