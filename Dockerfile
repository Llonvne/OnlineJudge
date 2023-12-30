FROM criyle/go-judge

RUN apt-get update && \
    apt-get install -y g++ --fix-missing && \
    rm -rf /var/lib/apt/lists/*

# 其他必要的设置或命令
