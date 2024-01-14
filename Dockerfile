FROM criyle/go-judge
RUN echo "deb https://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse" > /etc/apt/sources.list \
    && echo "deb-src https://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse" >> /etc/apt/sources.list
RUN apt-get update
RUN apt-get install -y g++ --fix-missing
RUN apt-get install -y openjdk-17-jdk --fix-missing
RUN rm -rf /var/lib/apt/lists/*
# 其他必要的设置或命令
