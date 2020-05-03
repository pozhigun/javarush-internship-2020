package com.space.controller;

import com.space.exception.BadRequestException;
import com.space.model.Ship;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ShipController {

    private final ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @PostMapping(value = "/rest/ships")
    @ResponseBody
    public Ship createShip(@RequestBody Ship ship) {
        Ship createShip = shipService.createShip(ship);
        if (createShip == null) {
            throw new BadRequestException();
        }

        return createShip;
    }

    @DeleteMapping(value = "/rest/ships/{id}")
    public void deleteShip(@PathVariable Long id) {
        if (!isIdValid(id)) {
            throw new BadRequestException();
        }

        shipService.delete(id);
    }

    @GetMapping(value = "/rest/ships/{id}")
    public Ship getShipById(@PathVariable Long id) {
        if (!isIdValid(id)) {
            throw new BadRequestException();
        }

        return shipService.getShipById(id);
    }

    @PostMapping(value = "/rest/ships/{id}")
    @ResponseBody
    public Ship updateShip(@RequestBody Ship ship, @PathVariable Long id) {
        if (!isIdValid(id)) {
            throw new BadRequestException();
        }

        return shipService.updateShip(ship, id);
    }

    private Boolean isIdValid(Long id) {
        return id != null &&
                id == Math.floor(id) &&
                id > 0;
    }
}