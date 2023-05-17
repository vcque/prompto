package com.vcque.prompto.lang.java;

import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PromptoJavaUtilsTest extends BasePlatformTestCase {


    @Test
    public void testAsPsiClass_fileCase() {
        var sourceCode = """
                package com.vcque.prompto.converter;
                                
                @ApplicationScoped
                @RequiredArgsConstructor
                public class DummyMessageConverter {
                                
                    private final ObjectMapper mapper;
                                
                    public TrainReadyMessage convert(NatsTrmMessage<?> natsTrmMessage) {
                        JsonNode raw = mapper.valueToTree(natsTrmMessage);
                        TrainStep step = buildTrainStep(natsTrmMessage);
                        TrainReadyProcess process = TrainReadyProcess.builder()
                                .id(natsTrmMessage.id().toString())
                                .step(step)
                                .build();
                        return TrainReadyMessage.builder()
                                .timestamp(natsTrmMessage.timestamp())
                                .status(TrainReadyStatus.REQUESTED)
                                .raw(raw)
                                .process(process)
                                .build();
                    }
                                
                    private TrainStep buildTrainStep(NatsTrmMessage<?> natsTrmMessage) {
                        return TrainStep.builder()
                                .id(natsTrmMessage.stepId())
                                .trainId(natsTrmMessage.trainId())
                                .departureDate(natsTrmMessage.departureDate())
                                .arrivalDate(natsTrmMessage.arrivalDate())
                                .departureStation(buildStation(natsTrmMessage.departureStation()))
                                .arrivalStation(buildStation(natsTrmMessage.arrivalStation()))
                                .build();
                    }
                                
                    private Station buildStation(NatsStation natsStation) {
                        return Station.builder()
                                .id(natsStation.id())
                                .label(natsStation.label())
                                .countryCode(natsStation.countryCode())
                                .ciCode(natsStation.ciCode())
                                .build();
                    }
                }
                """;

        var psiClass = PromptoJavaUtils.asPsiClass(getProject(), sourceCode);
        assertHasMethods(psiClass, 3);
    }

    @Test
    public void testAsPsiClass_classCase() {
        String code = "public class MyClass {}";
        var psiClass = PromptoJavaUtils.asPsiClass(getProject(), code);
        assertHasMethods(psiClass, 0);
    }

    @Test
    public void testAsPsiClass_methodCase() {
        String code = "public void test() {}";
        var psiClass = PromptoJavaUtils.asPsiClass(getProject(), code);
        assertHasMethods(psiClass, 1);
    }

    @Test
    public void testAsPsiClass_multipleMethodCase() {
        String code = """
                public void test1() {}
                public void test2() {}
                """;
        var psiClass = PromptoJavaUtils.asPsiClass(getProject(), code);
        assertHasMethods(psiClass, 2);
    }

    private static void assertHasMethods(PsiClass psiClass, int methodCount) {
        Assertions.assertThat(psiClass).isNotNull();
        assertEquals(methodCount, psiClass.getMethods().length);
    }
}