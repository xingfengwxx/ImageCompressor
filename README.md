# 图片优化

- 质量压缩
- 尺寸压缩
- 无损压缩

## Bitmap.compress()

```java
public boolean compress(Bitmap.CompressFormat format, int quality, OutputStream stream) ;
```

这个方法有三个参数：

- Bitmap.CompressFormat format 图像的压缩格式；
- int quality 图像压缩率，0-100。 0 压缩100%，100意味着不压缩；
- OutputStream stream 写入压缩数据的输出流；

返回值

- 如果成功地把压缩数据写入输出流，则返回true。


## inJustDecodeBounds

- Options中有个属性inJustDecodeBounds，我们可以充分利用它，来避免大图片的溢出问题。
- 如果该值设为true那么将不返回实际的bitmap，也不给其分配内存空间这样就避免内存溢出了。但是允许我们查询图片的信息，这其中就包括图片大小信息，options.outHeight (图片原始高度)和option.outWidth(图片原始宽度)。
- Options中有个属性inSampleSize，我们可以充分利用它，实现缩放，如果被设置为一个值，要求解码器解码出原始图像的一个子样本，返回一个较小的bitmap，以节省存储空间。例如，inSampleSize =  2，则取出的缩略图的宽和高都是原始图片的1/2，图片大小就为原始大小的1/4。

![inJustDecodeBounds](https://gitee.com/xingfengwxx/blogImage/raw/master/img/20211101162531.png)

## libjpeg

- libjpeg是一个完全用C语言编写的库，包含了被广泛使用的JPEG解码、JPEG编码和其他的JPEG功能的实现。

## libjpeg-turbo

- libjpeg-turbo图像编解码器，使用了SIMD指令（MMX，SSE2，NEON，AltiVec）来加速x86，x86-64，ARM和PowerPC系统上的JPEG压缩和解压缩。在这样的系统上，libjpeg-turbo的速度通常是libjpeg的2-6倍，其他条件相同。在其他类型的系统上，凭借其高度优化的霍夫曼编码，libjpeg-turbo仍然可以大大超过libjpeg。在许多情况下，libjpeg-turbo的性能可与专有的高速JPEG编解码器相媲美。

## 图片压缩流程

![图片压缩流程](https://gitee.com/xingfengwxx/blogImage/raw/master/img/20211101162642.png)

## 左移右移运算

### 左移操作（<<）

规则：

右边空出的位用0填补

高位左移溢出则舍弃该高位。

即 3 在32位计算机中的存储为(前后两条黑色竖线人为添加以方便于识别)：

```
| 0000 0000 0000 0000 0000 0000 0000 0011 |
```

左移2位结果如下：

```
00 | 00 0000 0000 0000 0000 0000 0000 0011 XX |
```

左移两位高位溢出，舍弃，低位也就是XX的位置空余，则补0变为：

```
| 0000 0000 0000 0000 0000 0000 0000 1100 |
```

再转换为十进制数：输出即为：12。

### 右移操作（>>）

左边空出的位用0或者1填补。正数用0填补，负数用1填补。

例如：6>>1

```
| 0000 0000 0000 0000 0000 0000 0000 0110 |
```

我们进行右移操作

```
| 0000 0000 0000 0000 0000 0000 0000 0011 | 0
```

则结果为 6>>1 = 3

## “与”运算

运算规则：0&0=0;0&1=0;1&0=0;1&1=1;

即：两位同时为“1”，结果才为“1”，否则为0


| 第一个输入 | 第二个输入 | 输出结果 |
| ---------- | ---------- | -------- |
| 1          | 1          | 1        |
| 1          | 0          | 0        |
| 0          | 1          | 0        |
| 0          | 0          | 0        |

## 颜色的二进制运算

![颜色的二进制运算](https://gitee.com/xingfengwxx/blogImage/raw/master/img/20211101163009.png)

## 内存上的像素读取

![内存上的像素读取](https://gitee.com/xingfengwxx/blogImage/raw/master/img/20211101163105.png)

