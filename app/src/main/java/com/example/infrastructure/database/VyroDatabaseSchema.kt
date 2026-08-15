package com.example.infrastructure.database

data class DatabaseTableSchema(
    val tableName: String,
    val description: String,
    val columns: List<DatabaseColumn>,
    val primaryKey: String,
    val indexes: List<String>,
    val foreignKeys: List<String>
)

data class DatabaseColumn(
    val name: String,
    val type: String,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
    val comment: String? = null
)

data class DatabaseMigration(
    val version: String,
    val description: String,
    val appliedAt: Long,
    val checksum: String,
    val sqlScript: String,
    val status: String = "SUCCESS"
)

object VyroDatabaseSchema {
    val TABLES: List<DatabaseTableSchema> = listOf(
        DatabaseTableSchema(
            tableName = "users",
            description = "Core user credentials and account identity",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("email", "VARCHAR(255) UNIQUE NOT NULL"),
                DatabaseColumn("password_hash", "VARCHAR(255) NOT NULL"),
                DatabaseColumn("password_salt", "VARCHAR(64) NOT NULL"),
                DatabaseColumn("role", "VARCHAR(32) NOT NULL DEFAULT 'VIEWER'"),
                DatabaseColumn("is_verified", "BOOLEAN DEFAULT FALSE"),
                DatabaseColumn("is_suspended", "BOOLEAN DEFAULT FALSE"),
                DatabaseColumn("two_factor_enabled", "BOOLEAN DEFAULT FALSE"),
                DatabaseColumn("created_at", "TIMESTAMPTZ NOT NULL DEFAULT NOW()"),
                DatabaseColumn("updated_at", "TIMESTAMPTZ NOT NULL DEFAULT NOW()")
            ),
            indexes = listOf("idx_users_email", "idx_users_role"),
            foreignKeys = emptyList()
        ),
        DatabaseTableSchema(
            tableName = "profiles",
            description = "User public profiles, bio, social links, and creator attributes",
            primaryKey = "user_id",
            columns = listOf(
                DatabaseColumn("user_id", "UUID PRIMARY KEY"),
                DatabaseColumn("username", "VARCHAR(50) UNIQUE NOT NULL"),
                DatabaseColumn("display_name", "VARCHAR(100) NOT NULL"),
                DatabaseColumn("avatar_url", "TEXT"),
                DatabaseColumn("banner_url", "TEXT"),
                DatabaseColumn("bio", "TEXT"),
                DatabaseColumn("country", "VARCHAR(64)"),
                DatabaseColumn("membership_tier", "VARCHAR(32) DEFAULT 'FREE'"),
                DatabaseColumn("subscribers_count", "BIGINT DEFAULT 0"),
                DatabaseColumn("total_views", "BIGINT DEFAULT 0")
            ),
            indexes = listOf("idx_profiles_username"),
            foreignKeys = listOf("FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "videos",
            description = "Long-form content, video transcripts, HLS master manifests and analytics",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("creator_id", "UUID NOT NULL"),
                DatabaseColumn("title", "VARCHAR(255) NOT NULL"),
                DatabaseColumn("description", "TEXT"),
                DatabaseColumn("category", "VARCHAR(64) NOT NULL"),
                DatabaseColumn("duration_seconds", "INT NOT NULL"),
                DatabaseColumn("master_hls_url", "TEXT NOT NULL"),
                DatabaseColumn("mpd_dash_url", "TEXT NOT NULL"),
                DatabaseColumn("thumbnail_url", "TEXT NOT NULL"),
                DatabaseColumn("views_count", "BIGINT DEFAULT 0"),
                DatabaseColumn("likes_count", "BIGINT DEFAULT 0"),
                DatabaseColumn("tips_total", "NUMERIC(14,2) DEFAULT 0.00"),
                DatabaseColumn("is_short", "BOOLEAN DEFAULT FALSE"),
                DatabaseColumn("visibility", "VARCHAR(32) DEFAULT 'PUBLIC'"),
                DatabaseColumn("published_at", "TIMESTAMPTZ NOT NULL DEFAULT NOW()")
            ),
            indexes = listOf("idx_videos_creator_id", "idx_videos_category", "idx_videos_published_at", "idx_videos_is_short"),
            foreignKeys = listOf("FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT")
        ),
        DatabaseTableSchema(
            tableName = "shorts",
            description = "Vertical high-retention 9:16 video micro-content",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("video_id", "UUID NOT NULL UNIQUE"),
                DatabaseColumn("audio_track_title", "VARCHAR(255)"),
                DatabaseColumn("loop_count", "BIGINT DEFAULT 0"),
                DatabaseColumn("aspect_ratio", "VARCHAR(16) DEFAULT '9:16'")
            ),
            indexes = listOf("idx_shorts_video_id"),
            foreignKeys = listOf("FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "comments",
            description = "User discussions, pinned comments, timestamps",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("video_id", "UUID NOT NULL"),
                DatabaseColumn("author_id", "UUID NOT NULL"),
                DatabaseColumn("content", "TEXT NOT NULL"),
                DatabaseColumn("likes_count", "INT DEFAULT 0"),
                DatabaseColumn("is_pinned", "BOOLEAN DEFAULT FALSE"),
                DatabaseColumn("created_at", "TIMESTAMPTZ NOT NULL DEFAULT NOW()")
            ),
            indexes = listOf("idx_comments_video_id", "idx_comments_author_id"),
            foreignKeys = listOf(
                "FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE",
                "FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE"
            )
        ),
        DatabaseTableSchema(
            tableName = "replies",
            description = "Nested comment discussion trees",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("parent_comment_id", "UUID NOT NULL"),
                DatabaseColumn("author_id", "UUID NOT NULL"),
                DatabaseColumn("content", "TEXT NOT NULL"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_replies_parent_comment_id"),
            foreignKeys = listOf(
                "FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE",
                "FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE"
            )
        ),
        DatabaseTableSchema(
            tableName = "likes",
            description = "Video and comment like associations",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("user_id", "UUID NOT NULL"),
                DatabaseColumn("video_id", "UUID NOT NULL"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_likes_user_video_unique (UNIQUE user_id, video_id)"),
            foreignKeys = listOf(
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE",
                "FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE"
            )
        ),
        DatabaseTableSchema(
            tableName = "follows",
            description = "Creator follower graph",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("follower_id", "UUID NOT NULL"),
                DatabaseColumn("creator_id", "UUID NOT NULL"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_follows_pair (UNIQUE follower_id, creator_id)"),
            foreignKeys = listOf(
                "FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE",
                "FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE"
            )
        ),
        DatabaseTableSchema(
            tableName = "subscriptions",
            description = "Paid creator VIP pass subscriptions and recurring billing",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("subscriber_id", "UUID NOT NULL"),
                DatabaseColumn("creator_id", "UUID NOT NULL"),
                DatabaseColumn("tier_id", "VARCHAR(32) NOT NULL"),
                DatabaseColumn("monthly_price", "NUMERIC(10,2) NOT NULL"),
                DatabaseColumn("status", "VARCHAR(32) DEFAULT 'ACTIVE'"),
                DatabaseColumn("renew_at", "TIMESTAMPTZ NOT NULL"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_subscriptions_subscriber", "idx_subscriptions_creator"),
            foreignKeys = listOf(
                "FOREIGN KEY (subscriber_id) REFERENCES users(id) ON DELETE CASCADE",
                "FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE"
            )
        ),
        DatabaseTableSchema(
            tableName = "communities",
            description = "Creator community hubs, forums, channels",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("creator_id", "UUID NOT NULL"),
                DatabaseColumn("name", "VARCHAR(100) NOT NULL"),
                DatabaseColumn("description", "TEXT"),
                DatabaseColumn("members_count", "BIGINT DEFAULT 0"),
                DatabaseColumn("is_exclusive_vip", "BOOLEAN DEFAULT FALSE"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_communities_creator_id"),
            foreignKeys = listOf("FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "messages",
            description = "Community chat messages and direct messages",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("community_id", "UUID"),
                DatabaseColumn("sender_id", "UUID NOT NULL"),
                DatabaseColumn("content", "TEXT NOT NULL"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_messages_community", "idx_messages_sender"),
            foreignKeys = listOf("FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "notifications",
            description = "In-app and push notification event stream",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("user_id", "UUID NOT NULL"),
                DatabaseColumn("type", "VARCHAR(64) NOT NULL"),
                DatabaseColumn("title", "VARCHAR(255) NOT NULL"),
                DatabaseColumn("body", "TEXT NOT NULL"),
                DatabaseColumn("target_url", "TEXT"),
                DatabaseColumn("is_read", "BOOLEAN DEFAULT FALSE"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_notifications_user_unread (user_id, is_read)"),
            foreignKeys = listOf("FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "reports",
            description = "Community moderation flags and content reports",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("reporter_id", "UUID NOT NULL"),
                DatabaseColumn("target_type", "VARCHAR(32) NOT NULL"), // VIDEO, COMMENT, USER
                DatabaseColumn("target_id", "UUID NOT NULL"),
                DatabaseColumn("reason", "VARCHAR(64) NOT NULL"),
                DatabaseColumn("details", "TEXT"),
                DatabaseColumn("status", "VARCHAR(32) DEFAULT 'PENDING'"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_reports_status", "idx_reports_target"),
            foreignKeys = listOf("FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "moderation_actions",
            description = "Audit trail of all administrative and automated AI moderation decisions",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("report_id", "UUID"),
                DatabaseColumn("moderator_id", "UUID NOT NULL"),
                DatabaseColumn("action_taken", "VARCHAR(64) NOT NULL"), // REMOVED, APPROVED, WARNED, SUSPENDED
                DatabaseColumn("notes", "TEXT"),
                DatabaseColumn("applied_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_moderation_moderator"),
            foreignKeys = listOf("FOREIGN KEY (moderator_id) REFERENCES users(id) ON DELETE RESTRICT")
        ),
        DatabaseTableSchema(
            tableName = "transactions_ledger",
            description = "Immutable double-entry financial ledger for all money flows",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("tx_hash", "VARCHAR(64) UNIQUE NOT NULL"),
                DatabaseColumn("from_account_id", "UUID NOT NULL"),
                DatabaseColumn("to_account_id", "UUID NOT NULL"),
                DatabaseColumn("amount", "NUMERIC(14,2) NOT NULL"),
                DatabaseColumn("currency", "VARCHAR(8) DEFAULT 'USD'"),
                DatabaseColumn("platform_fee", "NUMERIC(10,2) DEFAULT 0.00"),
                DatabaseColumn("creator_net", "NUMERIC(14,2) NOT NULL"),
                DatabaseColumn("type", "VARCHAR(64) NOT NULL"),
                DatabaseColumn("status", "VARCHAR(32) DEFAULT 'SETTLED'"),
                DatabaseColumn("reference_id", "VARCHAR(128)"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_ledger_from_account", "idx_ledger_to_account", "idx_ledger_tx_hash", "idx_ledger_created_at"),
            foreignKeys = emptyList()
        ),
        DatabaseTableSchema(
            tableName = "creator_earnings",
            description = "Aggregated daily/monthly creator earnings rollups and payouts",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("creator_id", "UUID NOT NULL"),
                DatabaseColumn("period_date", "DATE NOT NULL"),
                DatabaseColumn("ad_revenue", "NUMERIC(12,2) DEFAULT 0.00"),
                DatabaseColumn("tips_revenue", "NUMERIC(12,2) DEFAULT 0.00"),
                DatabaseColumn("subscription_revenue", "NUMERIC(12,2) DEFAULT 0.00"),
                DatabaseColumn("marketplace_revenue", "NUMERIC(12,2) DEFAULT 0.00"),
                DatabaseColumn("total_net_payout", "NUMERIC(12,2) DEFAULT 0.00")
            ),
            indexes = listOf("idx_creator_earnings_date (creator_id, period_date)"),
            foreignKeys = listOf("FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "products",
            description = "Creator marketplace commerce items",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("creator_id", "UUID NOT NULL"),
                DatabaseColumn("title", "VARCHAR(255) NOT NULL"),
                DatabaseColumn("description", "TEXT"),
                DatabaseColumn("price", "NUMERIC(10,2) NOT NULL"),
                DatabaseColumn("currency", "VARCHAR(8) DEFAULT 'USD'"),
                DatabaseColumn("inventory_count", "INT DEFAULT 100"),
                DatabaseColumn("product_type", "VARCHAR(32) DEFAULT 'DIGITAL'"),
                DatabaseColumn("is_active", "BOOLEAN DEFAULT TRUE")
            ),
            indexes = listOf("idx_products_creator"),
            foreignKeys = listOf("FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "orders",
            description = "Customer commerce orders & product fulfillment",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("product_id", "UUID NOT NULL"),
                DatabaseColumn("buyer_id", "UUID NOT NULL"),
                DatabaseColumn("price_paid", "NUMERIC(10,2) NOT NULL"),
                DatabaseColumn("fulfillment_status", "VARCHAR(32) DEFAULT 'COMPLETED'"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_orders_buyer", "idx_orders_product"),
            foreignKeys = listOf(
                "FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT",
                "FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE RESTRICT"
            )
        ),
        DatabaseTableSchema(
            tableName = "advertisements",
            description = "Ad campaign inventory, target categories, and impressions",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("advertiser_id", "UUID NOT NULL"),
                DatabaseColumn("title", "VARCHAR(255) NOT NULL"),
                DatabaseColumn("media_url", "TEXT NOT NULL"),
                DatabaseColumn("target_category", "VARCHAR(64)"),
                DatabaseColumn("budget_total", "NUMERIC(12,2) NOT NULL"),
                DatabaseColumn("cpm_rate", "NUMERIC(8,2) NOT NULL"),
                DatabaseColumn("status", "VARCHAR(32) DEFAULT 'ACTIVE'")
            ),
            indexes = listOf("idx_ads_category_status"),
            foreignKeys = listOf("FOREIGN KEY (advertiser_id) REFERENCES users(id) ON DELETE RESTRICT")
        ),
        DatabaseTableSchema(
            tableName = "ai_usage_logs",
            description = "Token usage, AI task audit logs, and cost accounting",
            primaryKey = "id",
            columns = listOf(
                DatabaseColumn("id", "UUID PRIMARY KEY"),
                DatabaseColumn("user_id", "UUID NOT NULL"),
                DatabaseColumn("task_type", "VARCHAR(64) NOT NULL"),
                DatabaseColumn("provider_adapter", "VARCHAR(64) NOT NULL"),
                DatabaseColumn("tokens_in", "INT NOT NULL"),
                DatabaseColumn("tokens_out", "INT NOT NULL"),
                DatabaseColumn("cost_usd", "NUMERIC(8,5) NOT NULL"),
                DatabaseColumn("created_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = listOf("idx_ai_usage_user", "idx_ai_usage_created_at"),
            foreignKeys = listOf("FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE")
        ),
        DatabaseTableSchema(
            tableName = "system_settings",
            description = "Dynamic platform configuration, active providers, flags",
            primaryKey = "key",
            columns = listOf(
                DatabaseColumn("key", "VARCHAR(128) PRIMARY KEY"),
                DatabaseColumn("value", "TEXT NOT NULL"),
                DatabaseColumn("data_type", "VARCHAR(32) DEFAULT 'STRING'"),
                DatabaseColumn("description", "TEXT"),
                DatabaseColumn("updated_at", "TIMESTAMPTZ DEFAULT NOW()")
            ),
            indexes = emptyList(),
            foreignKeys = emptyList()
        )
    )

    val MIGRATIONS: List<DatabaseMigration> = listOf(
        DatabaseMigration(
            version = "V1.0.0",
            description = "Initialize VYRO Independent Relational Core Schema (PostgreSQL)",
            appliedAt = 1723600000000L,
            checksum = "sha256:7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
            sqlScript = """
                -- VYRO PostgreSQL Core Schema Migration V1.0.0
                CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
                CREATE EXTENSION IF NOT EXISTS "pgcrypto";

                CREATE TABLE IF NOT EXISTS users (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    password_salt VARCHAR(64) NOT NULL,
                    role VARCHAR(32) NOT NULL DEFAULT 'VIEWER',
                    is_verified BOOLEAN DEFAULT FALSE,
                    is_suspended BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                );

                CREATE TABLE IF NOT EXISTS profiles (
                    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    display_name VARCHAR(100) NOT NULL,
                    avatar_url TEXT,
                    banner_url TEXT,
                    bio TEXT,
                    country VARCHAR(64),
                    membership_tier VARCHAR(32) DEFAULT 'FREE',
                    subscribers_count BIGINT DEFAULT 0,
                    total_views BIGINT DEFAULT 0
                );

                CREATE TABLE IF NOT EXISTS videos (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    creator_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    category VARCHAR(64) NOT NULL,
                    duration_seconds INT NOT NULL,
                    master_hls_url TEXT NOT NULL,
                    mpd_dash_url TEXT NOT NULL,
                    thumbnail_url TEXT NOT NULL,
                    views_count BIGINT DEFAULT 0,
                    likes_count BIGINT DEFAULT 0,
                    tips_total NUMERIC(14,2) DEFAULT 0.00,
                    is_short BOOLEAN DEFAULT FALSE,
                    visibility VARCHAR(32) DEFAULT 'PUBLIC',
                    published_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                );

                CREATE INDEX IF NOT EXISTS idx_videos_creator_id ON videos(creator_id);
                CREATE INDEX IF NOT EXISTS idx_videos_category ON videos(category);
                CREATE INDEX IF NOT EXISTS idx_videos_published_at ON videos(published_at DESC);
            """.trimIndent()
        ),
        DatabaseMigration(
            version = "V2.0.0",
            description = "Add Double-Entry Financial Ledger, AI Engine Logs, and Community Tables",
            appliedAt = 1723650000000L,
            checksum = "sha256:3e29f8a7c645b201f9c6d3a8274b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
            sqlScript = """
                CREATE TABLE IF NOT EXISTS transactions_ledger (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    tx_hash VARCHAR(64) UNIQUE NOT NULL,
                    from_account_id UUID NOT NULL,
                    to_account_id UUID NOT NULL,
                    amount NUMERIC(14,2) NOT NULL,
                    currency VARCHAR(8) DEFAULT 'USD',
                    platform_fee NUMERIC(10,2) DEFAULT 0.00,
                    creator_net NUMERIC(14,2) NOT NULL,
                    type VARCHAR(64) NOT NULL,
                    status VARCHAR(32) DEFAULT 'SETTLED',
                    reference_id VARCHAR(128),
                    created_at TIMESTAMPTZ DEFAULT NOW()
                );

                CREATE INDEX IF NOT EXISTS idx_ledger_hash ON transactions_ledger(tx_hash);
                CREATE INDEX IF NOT EXISTS idx_ledger_from ON transactions_ledger(from_account_id);
                CREATE INDEX IF NOT EXISTS idx_ledger_to ON transactions_ledger(to_account_id);

                CREATE TABLE IF NOT EXISTS ai_usage_logs (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    task_type VARCHAR(64) NOT NULL,
                    provider_adapter VARCHAR(64) NOT NULL,
                    tokens_in INT NOT NULL,
                    tokens_out INT NOT NULL,
                    cost_usd NUMERIC(8,5) NOT NULL,
                    created_at TIMESTAMPTZ DEFAULT NOW()
                );
            """.trimIndent()
        )
    )
}
