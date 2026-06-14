package com.example.dhap.dto.dashboard;

/** Aggregated counts for GET /dashboard/stats. */
public class DashboardStatsResponse {

    public Tasks      tasks      = new Tasks();
    public Volunteers volunteers = new Volunteers();
    public Requests   requests   = new Requests();
    public Resources  resources  = new Resources();

    public static class Tasks {
        public long total;
        public long pending;
        public long inProgress;
        public long inVerification;
        public long completed;
    }

    public static class Volunteers {
        public long active;   // inTask == true
        public long total;    // role == VOLUNTEER
    }

    public static class Requests {
        public long total;
        public long pending;
        public long accepted;
    }

    public static class Resources {
        public long total;
        public long food;
        public long water;
        public long medicine;  // backed by resourceType == "MEDICINE"
        public long shelter;
    }
}
