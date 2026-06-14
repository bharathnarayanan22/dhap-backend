package com.example.dhap.entities;

import java.util.ArrayList;
import java.util.List;

/** Embedded proof inside Task.proofs — not a separate collection. */
public class Proof {
    public String       message;
    public List<String> mediaPaths = new ArrayList<>();

    public Proof() {}
}