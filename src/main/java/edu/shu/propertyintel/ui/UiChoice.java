package edu.shu.propertyintel.ui;

record UiChoice<T>(String label, T value) {
    @Override public String toString() { return label; }
}
