export interface Codable<Value, Format> {
  encode(value: Value): Format;
  decode(data: Format): Value;
  typeName: string;
}

export type HasCodec<Value, Format> = Value & { codable: Codec<Value, Format> };

export declare function jungle<Format, Value extends HasCodec<Value, Format>>(msg: T): T;
