package br.com.automacaowebia.model;

public class DashResumo {

    private final long totalTemplates;
    private final long totalLabels;
    private final long totalJobs;
    private final long totalDia;
    private final long totalMes;
    private final long totalAno;

    public DashResumo(long totalTemplates, long totalLabels, long totalJobs,
                      long totalDia, long totalMes, long totalAno) {
        this.totalTemplates = totalTemplates;
        this.totalLabels = totalLabels;
        this.totalJobs = totalJobs;
        this.totalDia = totalDia;
        this.totalMes = totalMes;
        this.totalAno = totalAno;
    }

    public long getTotalTemplates() { return totalTemplates; }
    public long getTotalLabels() { return totalLabels; }
    public long getTotalJobs() { return totalJobs; }
    public long getTotalDia() { return totalDia; }
    public long getTotalMes() { return totalMes; }
    public long getTotalAno() { return totalAno; }

    @Override
    public String toString() {
        return "DashResumo{" +
                "totalTemplates=" + totalTemplates +
                ", totalLabels=" + totalLabels +
                ", totalJobs=" + totalJobs +
                ", totalDia=" + totalDia +
                ", totalMes=" + totalMes +
                ", totalAno=" + totalAno +
                '}';
    }
}
