package edu.shu.propertyintel.model;

import java.time.LocalDate;

public record Sale(String id, String propertyId, LocalDate date, double price) {
}
