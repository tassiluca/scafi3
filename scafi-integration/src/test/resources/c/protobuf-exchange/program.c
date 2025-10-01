#include "messages.pb-c.h"

static uint8_t* temp_sensor_encode(void* data, size_t* encoded_size) {
    if (!data || !encoded_size) return NULL;
    TemperatureSensor* message = (TemperatureSensor*)data;
    *encoded_size = protobuf_c_message_get_packed_size(&message->base);
    uint8_t* buffer = malloc(*encoded_size);
    if (!buffer) return NULL;
    size_t packed_size = protobuf_c_message_pack(&message->base, buffer);
    // Verify the pack succeeded as expected
    if (packed_size != *encoded_size) {
        free(buffer);
        return NULL;
    }
    return buffer;
}

static char* temp_sensor_to_str(const void* data) {
    if (!data) return NULL;
    BinaryCodable* bc = (BinaryCodable*) data;
    TemperatureSensor* value = (TemperatureSensor*) bc->data;
    size_t needed = snprintf(NULL, 0, "Sensor(id=%s, temp=%.2f)", value->id, value->temperature) + 1;
    char* str = malloc(needed);
    if (!str) return NULL;
    snprintf(str, needed, "Sensor(id=%s, temp=%.2f)", value->id, value->temperature);
    return str;
}

static bool temp_sensor_compare(const void* a, const void* b) {
    const BinaryCodable* v1 = (const BinaryCodable*)a;
    const BinaryCodable* v2 = (const BinaryCodable*)b;
    return strcmp(((TemperatureSensor*)v1->data)->id, ((TemperatureSensor*)v2->data)->id) == 0 &&
           ((TemperatureSensor*)v1->data)->temperature == ((TemperatureSensor*)v2->data)->temperature;
}

static BinaryCodable* temp_sensor_decode(const uint8_t* buffer, size_t size) {
    if (!buffer || size == 0) return NULL;
    TemperatureSensor* temp = (TemperatureSensor*) protobuf_c_message_unpack(
        &temperature_sensor__descriptor, NULL, size, buffer
    );
    if (!temp) return NULL;
    BinaryCodable *bc = malloc(sizeof(BinaryCodable));
    if (!bc) {
        protobuf_c_message_free_unpacked((ProtobufCMessage*)temp, NULL);
        return NULL;
    }
    bc->data = temp;  // Use the unpacked message directly
    bc->type_name = strdup(temp->base.descriptor->name);  // Create a copy
    bc->encode = temp_sensor_encode;
    bc->decode = temp_sensor_decode;
    bc->equals = temp_sensor_compare;
    bc->to_str = temp_sensor_to_str;
    return bc;
}

static BinaryCodable* codable_temp_sensor(TemperatureSensor* value) {
    BinaryCodable* bc = malloc(sizeof(BinaryCodable));
    if (!bc) return NULL;
    bc->data = value;
    bc->type_name = strdup(value->base.descriptor->name);
    if (!bc->type_name) {
        free(bc);
        return NULL;
    }
    bc->encode = temp_sensor_encode;
    bc->decode = temp_sensor_decode;
    bc->equals = temp_sensor_compare;
    bc->to_str = temp_sensor_to_str;
    return bc;
}

BinaryCodable* sense_temperature() {
    TemperatureSensor* sensor = malloc(sizeof(TemperatureSensor));
    if (!sensor) return NULL;
    temperature_sensor__init(sensor);
    int id_len = snprintf(NULL, 0, "#%d", {{ deviceId }}) + 1; // +1 for null terminator
    sensor->id = malloc(10);
    if (!sensor->id) {
        free(sensor);
        return NULL;
    }
    snprintf(sensor->id, id_len, "#%d", {{ deviceId }});
    sensor->temperature = {{ deviceId }} * 10.0f;
    return codable_temp_sensor(sensor);
}

static void temp_sensor_free(void* data) {
    if (!data) return;
    BinaryCodable* bc = (BinaryCodable*)data;
    if (bc->data) {
        TemperatureSensor* sensor = (TemperatureSensor*)bc->data;
        free(sensor->id); // Free the id string if it was malloc'd
        free(sensor);
    }
    // Only free type_name if it was strdup'd (need to track this)
    free(bc);
}

const void* aggregate_program(const AggregateLibrary* lang) {
    BinaryCodable* temperature = sense_temperature();
    return lang->exchange(lang->Field.of(temperature), lambda(ReturnSending, (const SharedData* in), {
        return return_sending(in, lang->Field.of(temperature));
    }));
}
