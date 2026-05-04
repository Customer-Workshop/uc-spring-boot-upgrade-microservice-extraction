package com.bank.rm.projection.service;

import com.bank.rm.common.dto.RiskCategory;
import com.bank.rm.projection.dto.ProjectionDtos.*;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class MonteCarloEngine {
    private static final int TOTAL_SCENARIOS = 10_000;
    private static final int BATCH_COUNT = 4;
    private static final double INFLATION_RATE = 0.06;

    private static final Map<String, double[]> ASSET_PARAMS = Map.of(
        "equity", new double[]{0.12, 0.18},
        "debt", new double[]{0.07, 0.04},
        "gold", new double[]{0.08, 0.12},
        "cash", new double[]{0.04, 0.01},
        "alternative", new double[]{0.15, 0.25}
    );

    public ProjectionResult runProjection(ProjectionInput input) {
        Map<String, Double> allocation = getAllocation(input.riskCategory());
        ExecutorService executor = Executors.newFixedThreadPool(BATCH_COUNT);
        int perBatch = TOTAL_SCENARIOS / BATCH_COUNT;
        List<Future<double[]>> futures = new ArrayList<>();

        for (int b = 0; b < BATCH_COUNT; b++) {
            int seed = b;
            futures.add(executor.submit(() -> runBatch(perBatch, input, allocation, seed)));
        }

        double[][] allFinalValues = new double[TOTAL_SCENARIOS][];
        double[][] yearlyMedians = new double[input.years()][TOTAL_SCENARIOS];
        int idx = 0;
        try {
            for (Future<double[]> f : futures) {
                double[] batchResults = f.get();
                for (int i = 0; i < perBatch; i++) {
                    allFinalValues[idx] = new double[]{batchResults[i]};
                    idx++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Monte Carlo computation failed", e);
        } finally {
            executor.shutdown();
        }

        double[] finalValues = Arrays.stream(allFinalValues).mapToDouble(a -> a[0]).toArray();
        Arrays.sort(finalValues);

        PercentileValues nominal = new PercentileValues(
            percentile(finalValues, 10), percentile(finalValues, 25),
            percentile(finalValues, 50), percentile(finalValues, 75),
            percentile(finalValues, 90)
        );

        double inflationFactor = Math.pow(1 + INFLATION_RATE, input.years());
        PercentileValues real = new PercentileValues(
            nominal.p10() / inflationFactor, nominal.p25() / inflationFactor,
            nominal.p50() / inflationFactor, nominal.p75() / inflationFactor,
            nominal.p90() / inflationFactor
        );

        double targetProb = input.targetAmount() > 0 ?
            (Arrays.stream(finalValues).filter(v -> v >= input.targetAmount()).count() * 100.0 / TOTAL_SCENARIOS) : 0;

        return new ProjectionResult(nominal, real, TOTAL_SCENARIOS, Math.round(targetProb * 10.0) / 10.0);
    }

    private double[] runBatch(int count, ProjectionInput input, Map<String, Double> allocation, int seed) {
        Random random = new Random(seed * 42L);
        double[] results = new double[count];
        for (int s = 0; s < count; s++) {
            double portfolio = input.initialInvestment();
            for (int y = 0; y < input.years(); y++) {
                double yearReturn = 0;
                for (Map.Entry<String, Double> entry : allocation.entrySet()) {
                    double[] params = ASSET_PARAMS.getOrDefault(entry.getKey(), new double[]{0.05, 0.10});
                    NormalDistribution dist = new NormalDistribution(params[0], params[1]);
                    double assetReturn = dist.inverseCumulativeProbability(random.nextDouble());
                    yearReturn += entry.getValue() * assetReturn;
                }
                portfolio = portfolio * (1 + yearReturn) + input.annualContribution();
            }
            results[s] = Math.max(0, portfolio);
        }
        return results;
    }

    private Map<String, Double> getAllocation(RiskCategory category) {
        return switch (category) {
            case CONSERVATIVE -> Map.of("equity", 0.20, "debt", 0.50, "gold", 0.15, "cash", 0.10, "alternative", 0.05);
            case MODERATE -> Map.of("equity", 0.40, "debt", 0.30, "gold", 0.15, "cash", 0.05, "alternative", 0.10);
            case AGGRESSIVE -> Map.of("equity", 0.60, "debt", 0.15, "gold", 0.10, "cash", 0.05, "alternative", 0.10);
            case VERY_AGGRESSIVE -> Map.of("equity", 0.75, "debt", 0.05, "gold", 0.05, "cash", 0.00, "alternative", 0.15);
        };
    }

    private double percentile(double[] sorted, int p) {
        int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return Math.round(sorted[Math.max(0, index)] * 100.0) / 100.0;
    }
}
