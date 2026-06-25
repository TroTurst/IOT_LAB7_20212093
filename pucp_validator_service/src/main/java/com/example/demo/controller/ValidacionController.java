package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validar")
public class ValidacionController {

    @GetMapping("/alumno/{codigo}")
    public ResponseEntity<Boolean> validarAlumno(@PathVariable String codigo) {
        boolean valido = codigo.length() == 8
                && codigo.chars().allMatch(Character::isDigit)
                && codigo.startsWith("20");
        return ResponseEntity.ok(valido);
    }

    @GetMapping("/candado/{pin}")
    public ResponseEntity<Boolean> validarCandado(@PathVariable String pin) {
        boolean valido = pin.length() == 4
                && pin.chars().allMatch(Character::isDigit)
                && !tieneRepetidosConsecutivos(pin);
        return ResponseEntity.ok(valido);
    }

    private boolean tieneRepetidosConsecutivos(String pin) {
        for (int i = 0; i < pin.length() - 1; i++) {
            if (pin.charAt(i) == pin.charAt(i + 1)) {
                return true;
            }
        }
        return false;
    }
}
