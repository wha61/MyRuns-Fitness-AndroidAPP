package com.example.ActivityRecognition.classifier;

public class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N365207360(i);
        return p;
    }
    static double N365207360(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() <= 1.178065) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() > 1.178065) {
            p = WekaClassifier.N74f395241(i);
        }
        return p;
    }
    static double N74f395241(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 1;
        } else if (((Double) i[64]).doubleValue() <= 9.769709) {
            p = 1;
        } else if (((Double) i[64]).doubleValue() > 9.769709) {
            p = 2;
        }
        return p;
    }
}
