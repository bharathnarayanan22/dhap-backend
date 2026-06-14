package com.example.dhap.dto.task;

import com.example.dhap.entities.Proof;
import java.util.List;

/** Used both in the request body (new proofs to add) and in TaskResponse. */
public class ProofDto {
    public String       message;
    public List<String> mediaPaths;

    public ProofDto() {}

    public static ProofDto from(Proof proof) {
        ProofDto dto  = new ProofDto();
        dto.message   = proof.message;
        dto.mediaPaths = proof.mediaPaths;
        return dto;
    }
}