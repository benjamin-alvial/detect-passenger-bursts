# detect-passenger-bursts
Apache Kafka code for detecting bursts of boarding passengers in bus stops in Santiago's 506 bus route.

# Projects Notes

---> Espacio de trabajo
LFS /data/2024/uhadoop/projects/group_22
HDFS /uhadoop2024/projects/group_22

---> Para subir la data
Option 1: download locally and transfer via SCP
Option 2: download directly to cluster-01 with curl or wget; transfer to HDFS; delete from cluster-01 ***

---> Data utilizada en [https://www.dtpm.cl/index.php/documentos/matrices-de-viaje](https://www.dtpm.cl/index.php/documentos/matrices-de-viaje)
Abril (días 17, 18, 19, 20, 21, 22 y 23)
Tablas de Subidas y Bajadas
Tabla de Viajes (.csv) y Estructura de Tabla de Viajes (.txt)
Tabla de Etapas (.csv)  y Estructura de Tabla de Etapas (.txt) ***

---> Enlace de archivo escogido (etapas)
[https://www.dtpm.cl/descargas/modelos_y_matrices/etapas_042023_17al23_transparencia.zip](https://www.dtpm.cl/descargas/modelos_y_matrices/etapas_042023_17al23_transparencia.zip)

## Comandos para correr proyecto.

Copiar .jar a servidor (LFS), desde terminal local
scp -P 220 C:\Users\benja\Desktop\detect-passenger-bursts\mdp-kafka\dist\mdp-kafka.jar uhadoop@cm.dcc.uchile.cl:/data/2024/uhadoop/projects/group_22/kafka

Copiar diccionario a servidor (LFS), desde terminal local
scp -P 220 C:\Users\benja\Downloads\diccionario_codigo_paradero_final.csv uhadoop@cm.dcc.uchile.cl:/data/2024/uhadoop/projects/group_22/

**Crear topic**
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic boardings

**Correr el .jar (NO FUNCIONA DIRECTO CON DATA EN HDFS)**
java -jar mdp-kafka.jar TwitterSimulator hdfs://cm:9000/uhadoop2024/projects/group_22/sorting/out4/part-r-00000 boardings 1000
java -jar mdp-kafka.jar PrintEarthquakeTweets boardings

**OTRA OPCIÓN: COPIAR AL LFS**
mkdir /data/2024/uhadoop/projects/group_22/full
hdfs dfs -get /uhadoop2024/projects/group_22/sorting/full/part-r-00000 /data/2024/uhadoop/projects/group_22/full
java -jar mdp-kafka.jar TwitterSimulator /data/2024/uhadoop/projects/group_22/testing/part-r-00000 boardings 1000
java -jar mdp-kafka.jar PrintEarthquakeTweets boardings

**Crear topic**
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic boardings-filtered

**Correr el .jar, usando tres terminales**
_Terminal 1:_
cd /data/2024/uhadoop/projects/group_22/kafka/
java -jar mdp-kafka.jar BurstDetector boardings-filtered

_Terminal 2:_
cd /data/2024/uhadoop/projects/group_22/kafka/
java -jar mdp-kafka.jar EarthquakeFilter boardings boardings-filtered

_Terminal 3:_
cd /data/2024/uhadoop/projects/group_22/kafka/
java -jar mdp-kafka.jar TwitterSimulator /data/2024/uhadoop/projects/group_22/full/part-r-00000 boardings 1000

_Nota: Se dejaron los nombres de las clases como en el lab 6 por problemas con el compilador._

# Results

```
START EVENT:  id 0 at 2023-04-17 07:56:12.000 on route 506v on stop PI545 (Arica / esq. Av. Padre A. Hurtado)
END EVENT: id 1 at rate 3 boardings in 22 min
START EVENT:  id 1 at 2023-04-17 15:06:02.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 2 at rate 3 boardings in 12 min
START EVENT:  id 2 at 2023-04-17 15:43:11.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 3 at rate 3 boardings in 9 min
START EVENT:  id 3 at 2023-04-17 17:04:43.000 on route 506v on stop PI546 (Arica / esq. Ob. Manuel Umaña)
END EVENT: id 4 at rate 3 boardings in 6 min
START EVENT:  id 4 at 2023-04-17 17:24:52.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 5 at rate 3 boardings in 3 min
START EVENT:  id 5 at 2023-04-17 17:50:56.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 6 at rate 3 boardings in 3 min
START EVENT:  id 6 at 2023-04-17 18:17:13.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 7 at rate 3 boardings in 7 min
START EVENT:  id 7 at 2023-04-17 18:24:47.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 8 at rate 3 boardings in 11 min
START EVENT:  id 8 at 2023-04-17 18:38:17.000 on route 506v on stop PA289 (Parada 1 / Molina - Blanco)
START EVENT:  id 9 at 2023-04-17 18:41:22.000 on route 506v on stop PA434 (Parada 1 / Plaza Ercilla)
END EVENT: id 10 at rate 3 boardings in 6 min
START EVENT:  id 10 at 2023-04-17 18:45:16.000 on route 506v on stop PA289 (Parada 1 / Molina - Blanco)
START EVENT:  id 11 at 2023-04-17 18:47:47.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 12 at rate 3 boardings in 11 min
END EVENT: id 12 at rate 3 boardings in 27 min
END EVENT: id 12 at rate 3 boardings in 42 min
START EVENT:  id 12 at 2023-04-18 06:24:23.000 on route 506v on stop PI1345 (Parada 4 / Nueva O'Higgins - Pajaritos)
END EVENT: id 13 at rate 3 boardings in 94 min
START EVENT:  id 13 at 2023-04-18 08:15:53.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 14 at rate 3 boardings in 15 min
START EVENT:  id 14 at 2023-04-18 09:31:11.000 on route 506v on stop PI545 (Arica / esq. Av. Padre A. Hurtado)
END EVENT: id 15 at rate 3 boardings in 12 min
START EVENT:  id 15 at 2023-04-18 10:10:12.000 on route 506v on stop PA290 (Parada 1 / Avenida España - Blanco)
END EVENT: id 16 at rate 3 boardings in 85 min
START EVENT:  id 16 at 2023-04-18 15:37:17.000 on route 506v on stop PA289 (Parada 1 / Molina - Blanco)
END EVENT: id 17 at rate 3 boardings in 20 min
START EVENT:  id 17 at 2023-04-18 16:40:56.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 18 at rate 3 boardings in 3 min
START EVENT:  id 18 at 2023-04-18 17:02:29.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 19 at rate 3 boardings in 5 min
START EVENT:  id 19 at 2023-04-18 17:43:26.000 on route 506v on stop PA290 (Parada 1 / Avenida España - Blanco)
START EVENT:  id 20 at 2023-04-18 18:07:51.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 21 at rate 3 boardings in 3 min
START EVENT:  id 21 at 2023-04-18 18:13:47.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 22 at rate 3 boardings in 3 min
START EVENT:  id 22 at 2023-04-18 18:35:16.000 on route 506v on stop PA289 (Parada 1 / Molina - Blanco)
END EVENT: id 23 at rate 3 boardings in 5 min
START EVENT:  id 23 at 2023-04-18 19:31:44.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 24 at rate 3 boardings in 2 min
START EVENT:  id 24 at 2023-04-18 19:52:06.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 25 at rate 3 boardings in 3 min
START EVENT:  id 25 at 2023-04-18 19:55:52.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 26 at rate 3 boardings in 6 min
END EVENT: id 26 at rate 3 boardings in 237 min
START EVENT:  id 26 at 2023-04-20 06:46:05.000 on route 506v on stop PI542 (Parada 1 / Colegio Villa España)
END EVENT: id 27 at rate 3 boardings in 22 min
START EVENT:  id 27 at 2023-04-20 13:24:13.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 28 at rate 3 boardings in 8 min
START EVENT:  id 28 at 2023-04-20 14:00:18.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 29 at rate 3 boardings in 10 min
START EVENT:  id 29 at 2023-04-20 14:51:13.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 30 at rate 3 boardings in 19 min
START EVENT:  id 30 at 2023-04-20 17:49:34.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 31 at rate 3 boardings in 2 min
START EVENT:  id 31 at 2023-04-20 18:44:22.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 32 at rate 3 boardings in 2 min
START EVENT:  id 32 at 2023-04-20 19:06:10.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 33 at rate 3 boardings in 8 min
START EVENT:  id 33 at 2023-04-20 19:47:32.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 34 at rate 3 boardings in 6 min
START EVENT:  id 34 at 2023-04-20 20:00:11.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 35 at rate 3 boardings in 3 min
START EVENT:  id 35 at 2023-04-20 20:17:48.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 36 at rate 3 boardings in 5 min
START EVENT:  id 36 at 2023-04-20 20:23:07.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 37 at rate 3 boardings in 3 min
START EVENT:  id 37 at 2023-04-20 20:52:01.000 on route 506v on stop PA433 (Parada 1 / Escuela de Ingeniería)
END EVENT: id 38 at rate 3 boardings in 7 min
START EVENT:  id 38 at 2023-04-21 10:39:27.000 on route 506v on stop PI534 (Parada 3 / Paradero 10 Pajaritos)
END EVENT: id 39 at rate 3 boardings in 28 min
```

