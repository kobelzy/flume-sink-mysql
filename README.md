#初始化批次配置文件
#\u7CFB\u7EDF\u914D\u7F6E\u4FEE\u6539
#Mon Apr 02 15:45:47 CST 2018
MONTH=201804
PHYSICS=1
LTE-SCENE=1
LTE-SITE=1
WCDMA-SITE=1
WCDMA-SCENE=1
GSM-SCENE=1
GSM-CELL=1
WCDMA_CELL=1
GSM-SITE=1
LTE-CELL=1



# flume-sink-mysql
配置文件
## Sources Definition for agent "agent"
#ACTIVE LIST
agent.sources = ftp
agent.channels = gsm-site gsm-cell wcdma-site wcdma_cell lte-site lte-cell physics gsm-scene wcdma-scene lte-scene
agent.sinks =  sink_gsm-site sink_gsm-cell sink_wcdma-site sink_wcdma-cell sink_lte-site sink_lte-cell sink_physics sink_gsm-scene sink_wcdma-scene sink_lte-scene


##配置source
agent.sources.ftp.type = org.lzy.flume.source.ftp.source.Source
agent.sources.ftp.client.source = ftp
agent.sources.ftp.name.server = 10.95.3.77
agent.sources.ftp.port = 21
agent.sources.ftp.user = hkd
agent.sources.ftp.password = 123456
agent.sources.ftp.working.directory = /home/hkd/
agent.sources.ftp.filter.pattern =.+(\\.csv|\\.xlsx)
agent.sources.ftp.folder = /var/log/flume-ftp
agent.sources.ftp.file.name = ftp-status-file.ser
agent.sources.ftp.run.discover.delay=5000
agent.sources.ftp.flushlines = true
agent.sources.ftp.table2fieldSize = GSM-SITE|61,GSM-CELL|56,WCDMA-SITE|72,WCDMA-CELL|80,LTE-SITE|66,LTE-CELL|81,PHYSICS|10,GSM-SCENE|13,WCDMA-SCENE|13,LTE-SCENE|13

agent.sources.ftp.selector.type=multiplexing
agent.sources.ftp.selector.header=flag
agent.sources.ftp.channels= gsm-site gsm-cell wcdma-site wcdma-cell lte-site lte-cell physics gsm-scene wcdma-scene lte-scene
agent.sources.ftp.selector.mapping.GSM-SITE= gsm-site 
agent.sources.ftp.selector.mapping.GSM-CELL= gsm-cell 
agent.sources.ftp.selector.mapping.WCDMA-SITE= wcdma-site 
agent.sources.ftp.selector.mapping.WCDMA-CELL= wcdma-cell 
agent.sources.ftp.selector.mapping.LTE-SITE= lte-site 
agent.sources.ftp.selector.mapping.LTE-CELL= lte-cell 
agent.sources.ftp.selector.mapping.PHYSICS= physics 
agent.sources.ftp.selector.mapping.GSM-SCENE= gsm-scene 
agent.sources.ftp.selector.mapping.WCDMA-SCENE= wcdma-scene 
agent.sources.ftp.selector.mapping.LTE-SCENE= lte-scene 
##此文件路径必须在源以及每个sink中都进行配置
agent.sources.ftp.inputBatchPath=/software/flume-ng/conf/input_batch.properties




#-----------------------------------######工参####---------------------------------------


####---------------------------No5--------------------------------                                            
#总配置
agent.sinks.sink_gsm-site.channel = gsm-site
#输出配置
agent.sinks.sink_gsm-site.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_gsm-site.hostname =10.95.3.112
agent.sinks.sink_gsm-site.port=3306
agent.sinks.sink_gsm-site.databaseName=resource_net
agent.sinks.sink_gsm-site.tableName=GSM_SITE
agent.sinks.sink_gsm-site.user=root
agent.sinks.sink_gsm-site.password=mysql
agent.sinks.sink_gsm-site.batchSize=1000
agent.sinks.sink_gsm-site.encodeFields=PROVINCE_NAME,CITY_NAME,AREA_NAME
agent.sinks.sink_gsm-site.encodeTableName=base_area
agent.sinks.sink_gsm-site.dictTableName=data_type_def
agent.sinks.sink_gsm-site.lossRecordTableName=loss_records
agent.sinks.sink_gsm-site.field2dictTableName=data_type_field2type
agent.sinks.sink_gsm-site.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.gsm-site.type = memory
agent.channels.gsm-site.capacity = 100000
agent.channels.gsm-site.transactionCapacity = 1000
agent.channels.gsm-site.keep-alive=60

####---------------------------No6--------------------------------
#总配置
agent.sinks.sink_gsm-cell.channel = gsm-cell
#输出配置
agent.sinks.sink_gsm-cell.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_gsm-cell.hostname =10.95.3.112
agent.sinks.sink_gsm-cell.port=3306
agent.sinks.sink_gsm-cell.databaseName=resource_net
agent.sinks.sink_gsm-cell.tableName=GSM_CELL
agent.sinks.sink_gsm-cell.user=root
agent.sinks.sink_gsm-cell.password=mysql
agent.sinks.sink_gsm-cell.batchSize=1000
agent.sinks.sink_gsm-cell.encodeFields=PROVINCE_NAME,CITY_NAME,AREA_NAME
agent.sinks.sink_gsm-cell.encodeTableName=base_area
agent.sinks.sink_gsm-cell.dictTableName=data_type_def
agent.sinks.sink_gsm-cell.lossRecordTableName=loss_records
agent.sinks.sink_gsm-cell.field2dictTableName=data_type_field2type
agent.sinks.sink_gsm-cell.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.gsm-cell.type = memory
agent.channels.gsm-cell.capacity = 100000
agent.channels.gsm-cell.transactionCapacity = 1000
agent.channels.gsm-cell.keep-alive=60

####---------------------------No7--------------------------------
#总配置
agent.sinks.sink_wcdma-site.channel = wcdma-site
#输出配置
agent.sinks.sink_wcdma-site.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_wcdma-site.hostname =10.95.3.112
agent.sinks.sink_wcdma-site.port=3306
agent.sinks.sink_wcdma-site.databaseName=resource_net
agent.sinks.sink_wcdma-site.tableName=WCDMA_SITE
agent.sinks.sink_wcdma-site.user=root
agent.sinks.sink_wcdma-site.password=mysql
agent.sinks.sink_wcdma-site.batchSize=1000
agent.sinks.sink_wcdma-site.encodeFields=PROVINCE_NAME,CITY_NAME,AREA_NAME
agent.sinks.sink_wcdma-site.encodeTableName=base_area
agent.sinks.sink_wcdma-site.dictTableName=data_type_def
agent.sinks.sink_wcdma-site.lossRecordTableName=loss_records
agent.sinks.sink_wcdma-site.field2dictTableName=data_type_field2type
agent.sinks.sink_wcdma-site.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.wcdma-site.type = memory
agent.channels.wcdma-site.capacity = 100000
agent.channels.wcdma-site.transactionCapacity = 1000
agent.channels.wcdma-site.keep-alive=60


####---------------------------No8--------------------------------
#总配置
agent.sinks.sink_wcdma-cell.channel = wcdma-cell
#输出配置
agent.sinks.sink_wcdma-cell.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_wcdma-cell.hostname =10.95.3.112
agent.sinks.sink_wcdma-cell.port=3306
agent.sinks.sink_wcdma-cell.databaseName=resource_net
agent.sinks.sink_wcdma-cell.tableName=WCDMA_CELL
agent.sinks.sink_wcdma-cell.user=root
agent.sinks.sink_wcdma-cell.password=mysql
agent.sinks.sink_wcdma-cell.batchSize=1000
agent.sinks.sink_wcdma-cell.encodeFields=PROVINCE_NAME,CITY_NAME,AREA_NAME
agent.sinks.sink_wcdma-cell.encodeTableName=base_area
agent.sinks.sink_wcdma-cell.dictTableName=data_type_def
agent.sinks.sink_wcdma-cell.lossRecordTableName=loss_records
agent.sinks.sink_wcdma-cell.field2dictTableName=data_type_field2type
agent.sinks.sink_wcdma-cell.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.wcdma-cell.type = memory
agent.channels.wcdma-cell.capacity = 100000
agent.channels.wcdma-cell.transactionCapacity = 1000
agent.channels.wcdma-cell.keep-alive=60


####---------------------------No9--------------------------------
#总配置
agent.sinks.sink_lte-site.channel = lte-site
#输出配置
agent.sinks.sink_lte-site.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_lte-site.hostname =10.95.3.112
agent.sinks.sink_lte-site.port=3306
agent.sinks.sink_lte-site.databaseName=resource_net
agent.sinks.sink_lte-site.tableName=LTE_SITE
agent.sinks.sink_lte-site.user=root
agent.sinks.sink_lte-site.password=mysql
agent.sinks.sink_lte-site.batchSize=1000
agent.sinks.sink_lte-site.encodeFields=PROVINCE_NAME,CITY_NAME,AREA_NAME
agent.sinks.sink_lte-site.encodeTableName=base_area
agent.sinks.sink_lte-site.dictTableName=data_type_def
agent.sinks.sink_lte-site.lossRecordTableName=loss_records
agent.sinks.sink_lte-site.field2dictTableName=data_type_field2type
agent.sinks.sink_lte-site.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.lte-site.type = memory
agent.channels.lte-site.capacity = 100000
agent.channels.lte-site.transactionCapacity = 1000
agent.channels.lte-site.keep-alive=60


####---------------------------No10--------------------------------
#总配置
agent.sinks.sink_lte-cell.channel = lte-cell
#输出配置
agent.sinks.sink_lte-cell.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_lte-cell.hostname =10.95.3.112
agent.sinks.sink_lte-cell.port=3306
agent.sinks.sink_lte-cell.databaseName=resource_net
agent.sinks.sink_lte-cell.tableName=LTE_CELL
agent.sinks.sink_lte-cell.user=root
agent.sinks.sink_lte-cell.password=mysql
agent.sinks.sink_lte-cell.batchSize=1000
agent.sinks.sink_lte-cell.encodeFields=PROVINCE_NAME,CITY_NAME,AREA_NAME
agent.sinks.sink_lte-cell.encodeTableName=base_area
agent.sinks.sink_lte-cell.dictTableName=data_type_def
agent.sinks.sink_lte-cell.lossRecordTableName=loss_records
agent.sinks.sink_lte-cell.field2dictTableName=data_type_field2type
agent.sinks.sink_lte-cell.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.lte-cell.type = memory
agent.channels.lte-cell.capacity = 100000
agent.channels.lte-cell.transactionCapacity = 1000
agent.channels.lte-cell.keep-alive=60

####---------------------------No11--------------------------------
#总配置
agent.sinks.sink_physics.channel = physics
#输出配置
agent.sinks.sink_physics.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_physics.hostname =10.95.3.112
agent.sinks.sink_physics.port=3306
agent.sinks.sink_physics.databaseName=resource_net
agent.sinks.sink_physics.tableName=PHYSICS_SITE
agent.sinks.sink_physics.user=root
agent.sinks.sink_physics.password=mysql
agent.sinks.sink_physics.batchSize=1000
agent.sinks.sink_physics.encodeFields=PROVINCE_NAME,CITY_NAME,AREA_NAME
agent.sinks.sink_physics.encodeTableName=base_area
agent.sinks.sink_physics.dictTableName=data_type_def
agent.sinks.sink_physics.lossRecordTableName=loss_records
agent.sinks.sink_physics.field2dictTableName=data_type_field2type
agent.sinks.sink_physics.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.physics.type = memory
agent.channels.physics.capacity = 100000
agent.channels.physics.transactionCapacity = 1000
agent.channels.physics.keep-alive=60


####---------------------------No12--------------------------------
#总配置
agent.sinks.sink_gsm-scene.channel = gsm-scene
#输出配置
agent.sinks.sink_gsm-scene.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_gsm-scene.hostname =10.95.3.112
agent.sinks.sink_gsm-scene.port=3306
agent.sinks.sink_gsm-scene.databaseName=resource_net
agent.sinks.sink_gsm-scene.tableName=SENCE_SOURCE
agent.sinks.sink_gsm-scene.user=root
agent.sinks.sink_gsm-scene.password=mysql
agent.sinks.sink_gsm-scene.batchSize=1000
agent.sinks.sink_gsm-scene.encodeFields=PROVINCE_NAME,CITY_NAME
agent.sinks.sink_gsm-scene.encodeTableName=base_area
agent.sinks.sink_gsm-scene.dictTableName=data_type_def
agent.sinks.sink_gsm-scene.lossRecordTableName=loss_records
agent.sinks.sink_gsm-scene.field2dictTableName=data_type_field2type
agent.sinks.sink_gsm-scene.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.gsm-scene.type = memory
agent.channels.gsm-scene.capacity = 100000
agent.channels.gsm-scene.transactionCapacity = 1000
agent.channels.gsm-scene.keep-alive=60

####---------------------------No13--------------------------------
#总配置
agent.sinks.sink_wcdma-scene.channel = wcdma-scene
#输出配置
agent.sinks.sink_wcdma-scene.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_wcdma-scene.hostname =10.95.3.112
agent.sinks.sink_wcdma-scene.port=3306
agent.sinks.sink_wcdma-scene.databaseName=resource_net
agent.sinks.sink_wcdma-scene.tableName=SENCE_SOURCE
agent.sinks.sink_wcdma-scene.user=root
agent.sinks.sink_wcdma-scene.password=mysql
agent.sinks.sink_wcdma-scene.batchSize=1000
agent.sinks.sink_wcdma-scene.encodeFields=PROVINCE_NAME,CITY_NAME
agent.sinks.sink_wcdma-scene.encodeTableName=base_area
agent.sinks.sink_wcdma-scene.dictTableName=data_type_def
agent.sinks.sink_wcdma-scene.lossRecordTableName=loss_records
agent.sinks.sink_wcdma-scene.field2dictTableName=data_type_field2type
agent.sinks.sink_wcdma-scene.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.wcdma-scene.type = memory
agent.channels.wcdma-scene.capacity = 100000
agent.channels.wcdma-scene.transactionCapacity = 1000
agent.channels.wcdma-scene.keep-alive=60


####---------------------------No14--------------------------------
#总配置
agent.sinks.sink_lte-scene.channel = lte-scene
#输出配置
agent.sinks.sink_lte-scene.type  = org.hkd.flume.sink.mysql.MysqlSink
agent.sinks.sink_lte-scene.hostname =10.95.3.112
agent.sinks.sink_lte-scene.port=3306
agent.sinks.sink_lte-scene.databaseName=resource_net
agent.sinks.sink_lte-scene.tableName=SENCE_SOURCE
agent.sinks.sink_lte-scene.user=root
agent.sinks.sink_lte-scene.password=mysql
agent.sinks.sink_lte-scene.batchSize=1000
agent.sinks.sink_lte-scene.encodeFields=PROVINCE_NAME,CITY_NAME
agent.sinks.sink_lte-scene.encodeTableName=base_area
agent.sinks.sink_lte-scene.dictTableName=data_type_def
agent.sinks.sink_lte-scene.lossRecordTableName=loss_records
agent.sinks.sink_lte-scene.field2dictTableName=data_type_field2type
agent.sinks.sink_lte-scene.inputBatchPath=/software/flume-ng/conf/input_batch.properties
#管道配置
agent.channels.lte-scene.type = memory
agent.channels.lte-scene.capacity = 100000
agent.channels.lte-scene.transactionCapacity = 1000
agent.channels.lte-scene.keep-alive=60
