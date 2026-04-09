package com.agrotech.system.application.port.out;

import com.agrotech.system.domain.model.Sensor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorPort {
    /**
     * Persiste um sensor.
     *
     * @param sensor sensor a ser salvo
     * @return sensor salvo
     */
    Sensor save(Sensor sensor);

    /**
     * Lista todos os sensores.
     *
     * @return lista de sensores
     */
    List<Sensor> findAll();

    /**
     * Lista sensores por areas.
     *
     * @param areaIds ids de areas
     * @return lista de sensores das areas informadas
     */
    List<Sensor> findAllByAreaIds(List<UUID> areaIds);

    /**
     * Busca um sensor pelo ID.
     *
     * @param sensorId ID do sensor
     * @return sensor, se existir
     */
    Optional<Sensor> findById(UUID sensorId);

    /**
     * Busca todos os sensores ativos.
     *
     * @return lista de sensores ativos
     */
    List<Sensor> findAllActive();

    /**
     * Remove um sensor pelo ID.
     *
     * @param sensorId id do sensor
     */
    void deleteById(UUID sensorId);
}

