package com.space.service;

import com.space.controller.ShipOrder;
import com.space.exception.BadRequestException;
import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipService {

    private final ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public Ship createShip(Ship ship) {
        if (ship.getName() == null ||
                ship.getName().isEmpty() ||
                ship.getName().length() > 50 ||
                ship.getPlanet() == null ||
                ship.getPlanet().isEmpty() ||
                ship.getPlanet().length() > 50 ||
                ship.getShipType() == null ||
                ship.getProdDate() == null ||
                ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() < 2800 ||
                ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() > 3019 ||
                ship.getSpeed() == null ||
                ship.getSpeed() < 0.01d ||
                ship.getSpeed() > 0.99d ||
                ship.getCrewSize() == null ||
                ship.getCrewSize() < 1 ||
                ship.getCrewSize() > 9999) {
            throw new BadRequestException();
        } else if (ship.getUsed() == null) {
            ship.setUsed(false);
        }

        ship.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);
        ship.setRating(computationRating(ship));
        return shipRepository.save(ship);
    }

    public void delete(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new NotFoundException();
        }

        shipRepository.deleteById(id);
    }

    public Ship getShipById(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new NotFoundException();
        }

        return shipRepository.getOne(id);
    }

    @Transactional
    public Ship updateShip(Ship newShip, Long id) {
        Ship shipUpdate = getShipById(id);

        if (newShip == null || shipUpdate == null) {
            throw new BadRequestException();
        }
        if (newShip.getName() != null) {
            if (newShip.getName().length() > 50 ||
                    newShip.getName().isEmpty()) {
                throw new BadRequestException();
            }
            shipUpdate.setName(newShip.getName());
        }
        if (newShip.getPlanet() != null) {
            if (newShip.getPlanet().length() > 50 ||
                    newShip.getPlanet().isEmpty()) {
                throw new BadRequestException();
            }
            shipUpdate.setPlanet(newShip.getPlanet());
        }
        if (newShip.getShipType() != null) {
            shipUpdate.setShipType(newShip.getShipType());
        }
        if (newShip.getProdDate() != null) {
            if (newShip.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() < 2800 ||
                    newShip.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() > 3019) {
                throw new BadRequestException();
            }
            shipUpdate.setProdDate(newShip.getProdDate());
        }
        if (newShip.getUsed() != null) {
            shipUpdate.setUsed(newShip.getUsed());
        }
        if (newShip.getSpeed() != null) {
            if (newShip.getSpeed() < 0.01d ||
                    newShip.getSpeed() > 0.99d) {
                throw new BadRequestException();
            }
            shipUpdate.setSpeed(newShip.getSpeed());
        }
        if (newShip.getCrewSize() != null) {
            if (newShip.getCrewSize() < 1 ||
                    newShip.getCrewSize() > 9999) {
                throw new BadRequestException();
            }
            shipUpdate.setCrewSize(newShip.getCrewSize());
        }

        shipUpdate.setRating(computationRating(shipUpdate));
        return shipRepository.save(shipUpdate);
    }

    public List<Ship> filteredShips(final List<Ship> filteredShips, ShipOrder order, Integer pageNumber, Integer pageSize) {
        pageNumber = pageNumber == null ? 0 : pageNumber;
        pageSize = pageSize == null ? 3 : pageSize;
        return filteredShips.stream()
                .sorted(getComparator(order))
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    private Comparator<Ship> getComparator(ShipOrder order) {
        if (order == null) {
            return Comparator.comparing(Ship::getId);
        }

        Comparator<Ship> comparator = null;
        switch (order.getFieldName()) {
            case "id":
                comparator = Comparator.comparing(Ship::getId);
                break;
            case "speed":
                comparator = Comparator.comparing(Ship::getSpeed);
                break;
            case "prodDate":
                comparator = Comparator.comparing(Ship::getProdDate);
                break;
            case "rating":
                comparator = Comparator.comparing(Ship::getRating);
        }

        return comparator;
    }

    private Double computationRating(Ship ship) {
        double speed = ship.getSpeed();
        double coefficientUsed = ship.getUsed() ? 0.5d : 1.0d;
        int currentYear = 3019;
        int productionDate = ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
        double rating = (80 * speed * coefficientUsed) / (double) (currentYear - productionDate + 1);
        return (double) Math.round(rating * 100) / 100;
    }
}