FROM criyle/go-judge

RUN apt-get update && apt-get install -y ca-certificates \
    && update-ca-certificates

# 安装GnuPG
RUN apt-get install -y gnupg

RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 3B4FE6ACC0B21F32 871920D1991BC93C

RUN echo "deb https://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse" > /etc/apt/sources.list \
    && echo "deb-src https://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse" >> /etc/apt/sources.list


RUN apt-get update && \
    apt-get install -y g++ --fix-missing && \
    apt-get install -y openjdk-17-jdk --fix-missing

RUN apt-get install python3.11 --fix-missing

RUN rm -rf /var/lib/apt/lists/*

# 其他必要的设置或命令
