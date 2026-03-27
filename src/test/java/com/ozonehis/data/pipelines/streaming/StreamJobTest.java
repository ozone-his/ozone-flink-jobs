package com.ozonehis.data.pipelines.streaming;

import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.KafkaStreamConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.QueryFile;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.internal.WhiteboxImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamJobTest {
	
	private static final String TEST_CFG_PATH = "/some/test/path";
	
	private static MockedStatic<CommonUtils> mockCommonUtils;
	
	private StreamJob job;
	
	@Mock
	private AppConfiguration mockConfig;
	
	@Mock
	private StreamTableEnvironment mockTableEnv;
	
	@BeforeEach
	void setup() {
		mockCommonUtils = Mockito.mockStatic(CommonUtils.class);
		when(CommonUtils.getConfig(TEST_CFG_PATH)).thenReturn(mockConfig);
		job = new StreamJob();
		WhiteboxImpl.setInternalState(job, "configFilePath", TEST_CFG_PATH);
		WhiteboxImpl.setInternalState(job, "tableEnv", mockTableEnv);
	}
	
	@AfterEach
	void tearDown() {
		mockCommonUtils.close();
	}
	
	@Test
	void beforeExecute_shouldConfigureKafkaTablesToResumeFromGroupOffsetsWithEarliestFallback() {
		KafkaStreamConfig kafkaStreamConfig = new KafkaStreamConfig();
		kafkaStreamConfig.setBootstrapServers("localhost:9092");
		kafkaStreamConfig.setTopicPrefix("emr.openmrs");
		kafkaStreamConfig.setTableDefinitionsPath("/sql/tables");
		QueryFile queryFile = new QueryFile(null, "encounter_diagnosis", "CREATE TABLE encounter_diagnosis ()");
		when(mockConfig.getKafkaStreams()).thenReturn(List.of(kafkaStreamConfig));
		when(CommonUtils.getSQL("/sql/tables")).thenReturn(List.of(queryFile));
		
		job.beforeExecute();
		
		ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(mockTableEnv).executeSql(queryCaptor.capture());
		String query = queryCaptor.getValue();
		assertAll(
				() -> Assertions.assertTrue(query.contains("'scan.startup.mode' = 'group-offsets'")),
				() -> Assertions.assertTrue(query.contains("'properties.auto.offset.reset' = 'earliest'")),
				() -> Assertions.assertTrue(query.contains("'properties.group.id' = 'encounter_diagnosis-group-id'")),
				() -> Assertions.assertTrue(query.contains("'topic' = 'emr.openmrs.encounter_diagnosis'")));
	}
}
