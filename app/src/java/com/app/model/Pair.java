/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.model;

/**
 *
 * @author G4T6
 * @param <F> a object input for the first member 
 * @param <S> a object input for the second member 
 */
public class Pair<F, S> {

    private F first; //first member of pair
    private S second; //second member of pair

    /**
     *Construct a Pair Object with the following parameters
     * @param first a object input for the first member 
     * @param second a object input for the second member 
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     *Sets the first member of the pair
     * @param first object to be set as first member
     */
    public void setFirst(F first) {
        this.first = first;
    }

    /**
     *Sets the second member of the pair
     * @param second object to be set as second member
     */
    public void setSecond(S second) {
        this.second = second;
    }

    /**
     *Gets the first member of the pair
     * @return the first member of pair
     */
    public F getFirst() {
        return first;
    }

    /**
     *Gets the second member of the pair
     * @return the second member of pair
     */
    public S getSecond() {
        return second;
    }
}
