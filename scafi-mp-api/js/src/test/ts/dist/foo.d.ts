import type { BinaryWriteOptions } from "@protobuf-ts/runtime";
import type { IBinaryWriter } from "@protobuf-ts/runtime";
import type { BinaryReadOptions } from "@protobuf-ts/runtime";
import type { IBinaryReader } from "@protobuf-ts/runtime";
import type { PartialMessage } from "@protobuf-ts/runtime";
import { MessageType } from "@protobuf-ts/runtime";
/**
 * @generated from protobuf message Foo
 */
export interface Foo {
    /**
     * @generated from protobuf field: string name = 1
     */
    name: string;
    /**
     * @generated from protobuf field: int32 id = 2
     */
    id: number;
}
declare class Foo$Type extends MessageType<Foo> {
    constructor();
    create(value?: PartialMessage<Foo>): Foo;
    internalBinaryRead(reader: IBinaryReader, length: number, options: BinaryReadOptions, target?: Foo): Foo;
    internalBinaryWrite(message: Foo, writer: IBinaryWriter, options: BinaryWriteOptions): IBinaryWriter;
}
/**
 * @generated MessageType for protobuf message Foo
 */
export declare const Foo: Foo$Type;
export {};
//# sourceMappingURL=foo.d.ts.map