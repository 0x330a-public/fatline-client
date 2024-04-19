
use jni::JNIEnv;
use jni::objects::{JByteArray, JClass};
use jni::sys::jbyteArray;

#[no_mangle]
extern "C" fn Java_online_mempool_fatline_data_Hash_hash(
    env: JNIEnv,
    _class: JClass,
    bytes: JByteArray,
) -> jbyteArray {
    let bytes = env.convert_byte_array(&bytes).expect("Couldn't convert jbyte array to vec");
    let hash = blake3::hash(&bytes);
    let mut truncated: [u8; 20] = [0u8; 20];
    truncated.copy_from_slice(&hash.as_bytes()[0..20]);
    let return_bytes = env.byte_array_from_slice(&truncated).expect("Couldn't convert vec to jbyte array");
    return return_bytes.into_raw();
}