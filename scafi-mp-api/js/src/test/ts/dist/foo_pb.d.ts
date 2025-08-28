import type { GenFile, GenMessage } from "@bufbuild/protobuf/codegenv2";
import type { Message } from "@bufbuild/protobuf";
/**
 * Describes the file foo.proto.
 */
export declare const file_foo: GenFile;
/**
 * @generated from message Foo
 */
export type Foo = Message<"Foo"> & {
    /**
     * @generated from field: string name = 1;
     */
    name: string;
    /**
     * @generated from field: int32 id = 2;
     */
    id: number;
};
/**
 * Describes the message Foo.
 * Use `create(FooSchema)` to create a new message.
 */
export declare const FooSchema: GenMessage<Foo>;
//# sourceMappingURL=foo_pb.d.ts.map