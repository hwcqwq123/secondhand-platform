# ==========================================
# 二手交易平台测试数据初始化脚本
# 功能：
# 1. 批量注册 20 个测试用户
# 2. 如果用户已存在，则自动登录
# 3. 更新用户昵称
# 4. 每个用户发布 1 个商品
#
# 运行前请确认：
# 1. backend 已启动：http://127.0.0.1:8080
# 2. audit-service 已启动：http://127.0.0.1:8001
# 3. 商品审核使用本地违禁词即可
# ==========================================

$BaseUrl = "http://127.0.0.1:8080/api"

# 解决中文乱码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$Users = @(
    @{
        username = "user001"
        password = "123456"
        nickname = "数码小陈"
        item = @{
            title = "二手蓝牙耳机九成新"
            board = "digital"
            price = 89.00
            description = "蓝牙耳机，音质正常，续航稳定，适合上课通勤使用，外观轻微使用痕迹。"
        }
    },
    @{
        username = "user002"
        password = "123456"
        nickname = "耳机玩家"
        item = @{
            title = "AirPods 二代耳机"
            board = "digital"
            price = 299.00
            description = "自用 AirPods 二代，功能正常，左右耳连接稳定，附保护壳。"
        }
    },
    @{
        username = "user003"
        password = "123456"
        nickname = "校园书屋"
        item = @{
            title = "高等数学教材上下册"
            board = "books"
            price = 35.00
            description = "高等数学教材上下册，笔记较少，适合大一学生预习和复习。"
        }
    },
    @{
        username = "user004"
        password = "123456"
        nickname = "小李同学"
        item = @{
            title = "宿舍折叠小桌子"
            board = "daily"
            price = 28.00
            description = "可折叠小桌子，适合宿舍床上学习、放电脑，桌面干净。"
        }
    },
    @{
        username = "user005"
        password = "123456"
        nickname = "运动达人"
        item = @{
            title = "羽毛球拍一副"
            board = "sports"
            price = 65.00
            description = "羽毛球拍一副，手柄完好，适合日常运动娱乐。"
        }
    },
    @{
        username = "user006"
        password = "123456"
        nickname = "小王闲置"
        item = @{
            title = "iPhone 手机壳三件套"
            board = "digital"
            price = 19.00
            description = "iPhone 手机壳三件套，透明壳和磨砂壳都有，适合日常替换。"
        }
    },
    @{
        username = "user007"
        password = "123456"
        nickname = "考研资料站"
        item = @{
            title = "考研英语真题资料"
            board = "books"
            price = 42.00
            description = "考研英语真题资料，部分有笔记，适合备考同学使用。"
        }
    },
    @{
        username = "user008"
        password = "123456"
        nickname = "桌面好物"
        item = @{
            title = "罗技无线鼠标"
            board = "digital"
            price = 58.00
            description = "罗技无线鼠标，按键灵敏，连接稳定，适合办公和学习。"
        }
    },
    @{
        username = "user009"
        password = "123456"
        nickname = "宿舍收纳家"
        item = @{
            title = "宿舍桌面收纳盒"
            board = "daily"
            price = 18.00
            description = "桌面收纳盒，可放文具、数据线、化妆品，空间利用率高。"
        }
    },
    @{
        username = "user010"
        password = "123456"
        nickname = "骑行同学"
        item = @{
            title = "校园代步自行车"
            board = "sports"
            price = 180.00
            description = "自行车适合校园代步，刹车正常，车身有轻微划痕。"
        }
    },
    @{
        username = "user011"
        password = "123456"
        nickname = "电子旧物铺"
        item = @{
            title = "二手 iPad 保护套"
            board = "digital"
            price = 25.00
            description = "iPad 保护套，带支架功能，适合看网课和记笔记。"
        }
    },
    @{
        username = "user012"
        password = "123456"
        nickname = "摄影社小周"
        item = @{
            title = "相机三脚架"
            board = "digital"
            price = 75.00
            description = "轻便三脚架，适合手机和相机拍摄，收纳方便。"
        }
    },
    @{
        username = "user013"
        password = "123456"
        nickname = "校园生活馆"
        item = @{
            title = "小型台灯"
            board = "daily"
            price = 30.00
            description = "宿舍学习台灯，亮度可调，适合晚上看书使用。"
        }
    },
    @{
        username = "user014"
        password = "123456"
        nickname = "英语学习屋"
        item = @{
            title = "四六级英语词汇书"
            board = "books"
            price = 20.00
            description = "四六级英语词汇书，书页完整，适合备考英语四六级。"
        }
    },
    @{
        username = "user015"
        password = "123456"
        nickname = "电脑配件铺"
        item = @{
            title = "机械键盘青轴"
            board = "digital"
            price = 120.00
            description = "机械键盘青轴，按键声音清脆，适合打字和日常使用。"
        }
    },
    @{
        username = "user016"
        password = "123456"
        nickname = "游戏外设君"
        item = @{
            title = "游戏手柄"
            board = "digital"
            price = 88.00
            description = "游戏手柄，按键正常，连接稳定，适合电脑游戏使用。"
        }
    },
    @{
        username = "user017"
        password = "123456"
        nickname = "女生宿舍闲置"
        item = @{
            title = "宿舍衣物收纳袋"
            board = "daily"
            price = 16.00
            description = "衣物收纳袋，容量较大，适合换季衣物整理。"
        }
    },
    @{
        username = "user018"
        password = "123456"
        nickname = "毕业清仓"
        item = @{
            title = "毕业清仓书架"
            board = "daily"
            price = 55.00
            description = "小型书架，适合宿舍桌面或床边使用，毕业搬宿舍低价出。"
        }
    },
    @{
        username = "user019"
        password = "123456"
        nickname = "运动器材铺"
        item = @{
            title = "篮球一个"
            board = "sports"
            price = 45.00
            description = "篮球一个，弹性正常，适合操场日常练习。"
        }
    },
    @{
        username = "user020"
        password = "123456"
        nickname = "学长闲置铺"
        item = @{
            title = "二手笔记本散热支架"
            board = "digital"
            price = 35.00
            description = "笔记本散热支架，可调节高度，适合宿舍学习和办公。"
        }
    }
)

function Convert-ToJsonBody($obj) {
    return ($obj | ConvertTo-Json -Depth 20)
}

function Invoke-ApiPost($url, $body, $headers = $null) {
    $json = Convert-ToJsonBody $body

    if ($headers -eq $null) {
        return Invoke-RestMethod `
            -Uri $url `
            -Method Post `
            -ContentType "application/json; charset=utf-8" `
            -Body $json
    } else {
        return Invoke-RestMethod `
            -Uri $url `
            -Method Post `
            -ContentType "application/json; charset=utf-8" `
            -Headers $headers `
            -Body $json
    }
}

function Invoke-ApiPut($url, $body, $headers) {
    $json = Convert-ToJsonBody $body

    return Invoke-RestMethod `
        -Uri $url `
        -Method Put `
        -ContentType "application/json; charset=utf-8" `
        -Headers $headers `
        -Body $json
}

function Register-Or-Login($username, $password) {
    Write-Host "----------------------------------------"
    Write-Host "处理账号：$username"

    $registerBody = @{
        username = $username
        password = $password
    }

    try {
        $registerRes = Invoke-ApiPost "$BaseUrl/auth/register" $registerBody

        if ($registerRes.success -eq $true) {
            Write-Host "注册成功：$username" -ForegroundColor Green
            return $registerRes.data.token
        } else {
            Write-Host "注册返回失败，尝试登录：$($registerRes.message)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "注册失败或账号已存在，尝试登录：$($_.Exception.Message)" -ForegroundColor Yellow
    }

    $loginBody = @{
        username = $username
        password = $password
    }

    try {
        $loginRes = Invoke-ApiPost "$BaseUrl/auth/login" $loginBody

        if ($loginRes.success -eq $true) {
            Write-Host "登录成功：$username" -ForegroundColor Green
            return $loginRes.data.token
        } else {
            Write-Host "登录失败：$($loginRes.message)" -ForegroundColor Red
            return $null
        }
    } catch {
        Write-Host "登录异常：$($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

function Update-Profile($token, $nickname) {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $body = @{
        nickname = $nickname
        avatarUrl = ""
        bio = "这是用于测试的校园二手平台账号：$nickname"
    }

    try {
        $res = Invoke-ApiPut "$BaseUrl/users/me" $body $headers

        if ($res.success -eq $true) {
            Write-Host "昵称更新成功：$nickname" -ForegroundColor Green
        } else {
            Write-Host "昵称更新失败：$($res.message)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "昵称更新异常：$($_.Exception.Message)" -ForegroundColor Yellow
    }
}

function Create-TestItem($token, $item) {
    $headers = @{
        Authorization = "Bearer $token"
    }

    # 注意：
    # imageUrls 留空，避免依赖图片上传接口。
    # coverIndex = 0，不影响无图商品发布。
    $body = @{
        title = $item.title
        description = $item.description
        price = $item.price
        board = $item.board
        imageUrls = @()
        coverIndex = 0
    }

    try {
        $res = Invoke-ApiPost "$BaseUrl/items" $body $headers

        if ($res.success -eq $true) {
            Write-Host "商品发布成功：$($item.title)" -ForegroundColor Green
        } else {
            Write-Host "商品发布失败：$($item.title) - $($res.message)" -ForegroundColor Red
        }
    } catch {
        Write-Host "商品发布异常：$($item.title) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "开始批量生成测试账号与商品..." -ForegroundColor Cyan
Write-Host "后端地址：$BaseUrl" -ForegroundColor Cyan
Write-Host ""

foreach ($u in $Users) {
    $token = Register-Or-Login $u.username $u.password

    if ($null -eq $token -or $token -eq "") {
        Write-Host "跳过用户：$($u.username)" -ForegroundColor Red
        continue
    }

    Update-Profile $token $u.nickname
    Create-TestItem $token $u.item
}

Write-Host ""
Write-Host "测试数据生成完成。" -ForegroundColor Cyan
Write-Host "你现在可以打开前端测试：" -ForegroundColor Cyan
Write-Host "1. 搜索：耳机、教材、鼠标、自行车、台灯、篮球" -ForegroundColor Cyan
Write-Host "2. AI 客服输入：有没有耳机 / 推荐一本高数教材 / 有没有自行车" -ForegroundColor Cyan
Write-Host "3. 使用不同账号登录，测试联系卖家和聊天功能" -ForegroundColor Cyan